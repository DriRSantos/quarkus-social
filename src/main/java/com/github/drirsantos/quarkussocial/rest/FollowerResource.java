package com.github.drirsantos.quarkussocial.rest;

import com.github.drirsantos.quarkussocial.domain.model.Follower;
import com.github.drirsantos.quarkussocial.domain.repository.FollowerRepository;
import com.github.drirsantos.quarkussocial.domain.repository.UserRepository;
import com.github.drirsantos.quarkussocial.rest.dto.FollowerRequest;
import com.github.drirsantos.quarkussocial.rest.dto.FollowerResponse;
import com.github.drirsantos.quarkussocial.rest.dto.FollowersPerUserResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository repository;
    private UserRepository userRepository;

    @Inject
    public FollowerResource(
            FollowerRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId) {
        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<Follower> list = repository.findByUser(userId);

        FollowersPerUserResponse responseObject = new FollowersPerUserResponse();
        responseObject.setFollowersCount(list.size());

        List<FollowerResponse> followersList = list.stream()
                .map(FollowerResponse::new).collect(Collectors.toList());

        responseObject.setContent(followersList);

        return Response.ok(responseObject).build();
    }

    @PUT
    @Transactional
    public Response followUser(
            @PathParam("userId") Long userId, FollowerRequest request) {

        if (userId.equals(request.getFollowerId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("You can't follow yourself").build();
        }
        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var follower = userRepository.findById(request.getFollowerId());
        boolean follows = repository.follows(follower, user);

        if (!follows) {
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);

            repository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId,
                                 @QueryParam("followerId") Long followerId) {
        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        repository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
