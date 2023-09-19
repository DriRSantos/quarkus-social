package com.github.drirsantos.quarkussocial.rest;

import com.github.drirsantos.quarkussocial.domain.model.User;
import com.github.drirsantos.quarkussocial.domain.repository.UserRepository;
import com.github.drirsantos.quarkussocial.rest.dto.CreateUserRequest;
import com.github.drirsantos.quarkussocial.rest.dto.ResponseError;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Set;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserRepository repository;
    private final Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator){
        this.repository = repository;
        this.validator = validator;
    }
    @GET
    public Response listAllUsers(){
        PanacheQuery<User> query = repository.findAll();
       return Response.ok(query.list()).build();
    }

    @POST
    @Transactional
    public Response createUser(CreateUserRequest userRequest){

        Set<ConstraintViolation<CreateUserRequest>> constraintViolations = validator.validate(userRequest);
        if(!constraintViolations.isEmpty()){
            return ResponseError.createFromValidation(constraintViolations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        User user = new User();
        user.setAge(userRequest.getAge());
        user.setName(userRequest.getName());
        repository.persist(user);

        return Response.status(Response.Status.CREATED.getStatusCode())
                .entity(user).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest userData){
        User user = repository.findById(id);
        if (user != null){
            user.setAge(userData.getAge());
            user.setName(userData.getName());
            return Response.noContent().build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("{idUser}")
    @Transactional
    public Response deleteUser(@PathParam("idUser") Long id){
        User user = repository.findById(id);

        if(user != null){
            repository.delete(user);
            return Response.noContent().build();
        }
        else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
