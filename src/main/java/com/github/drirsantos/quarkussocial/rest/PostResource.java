package com.github.drirsantos.quarkussocial.rest;

import com.github.drirsantos.quarkussocial.domain.model.Post;
import com.github.drirsantos.quarkussocial.domain.model.User;
import com.github.drirsantos.quarkussocial.domain.repository.FollowerRepository;
import com.github.drirsantos.quarkussocial.domain.repository.PostRepository;
import com.github.drirsantos.quarkussocial.domain.repository.UserRepository;
import com.github.drirsantos.quarkussocial.rest.dto.CreatePostRequest;
import com.github.drirsantos.quarkussocial.rest.dto.PostResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResource(UserRepository userRepository, PostRepository postRepository, FollowerRepository followerRepository){
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @GET
    public Response listPosts(@PathParam("userId") Long userId,
                              @HeaderParam("followerId") Long followerId){
        User user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(followerId == null){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("You forgot to pass the followerId on Header").build();
        }

        User follower = userRepository.findById(followerId);
        if(follower == null){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("FollowerId doesn't exist").build();
        }

        boolean follows = followerRepository.follows(follower, user);
        if(!follows){
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You can't see these posts, follow the user to see").build();
        }

        PanacheQuery<Post> query = postRepository.find(
                "user", Sort.by("dateTime", Sort.Direction.Descending), user);
        List<Post> list = query.list();

        // post -> PostResponse.fromEntity(post) substituted by method reference PostResponse::fromEntity
        List<PostResponse> postResponseList = list.stream()
                .map(PostResponse::fromEntity).collect(Collectors.toList());

        return Response.ok(postResponseList).build();
    }

    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long userId, CreatePostRequest createPostRequest){
        User user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Post post = new Post();
        post.setText(createPostRequest.getText());
        post.setUser(user);

        postRepository.persist(post);

        return Response.status(Response.Status.CREATED).build();
    }
}
