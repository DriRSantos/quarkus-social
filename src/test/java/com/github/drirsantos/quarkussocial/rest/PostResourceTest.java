package com.github.drirsantos.quarkussocial.rest;

import com.github.drirsantos.quarkussocial.domain.model.Follower;
import com.github.drirsantos.quarkussocial.domain.model.Post;
import com.github.drirsantos.quarkussocial.domain.model.User;
import com.github.drirsantos.quarkussocial.domain.repository.FollowerRepository;
import com.github.drirsantos.quarkussocial.domain.repository.PostRepository;
import com.github.drirsantos.quarkussocial.domain.repository.UserRepository;
import com.github.drirsantos.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUp(){
        var user = new User();
        user.setAge(14);
        user.setName("Dri");
        userRepository.persist(user);
        userId = user.getId();

        Post post = new Post();
        post.setUser(user);
        post.setText("Hello World Dri");
        postRepository.persist(post);


        var userNotFollower = new User();
        userNotFollower.setAge(20);
        userNotFollower.setName("Teste");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        var userFollower = new User();
        userFollower.setAge(25);
        userFollower.setName("Fulano");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);
    }

    @Test
    @DisplayName("should create a post a user")
    public void createPostTest() {

        var postRequest = new CreatePostRequest();
        postRequest.setText("Hello World Dri");

        var userId = 1;

        var response = given()
                    .contentType(ContentType.JSON)
                    .body(postRequest)
                    .pathParams("userId", userId)
                .when()
                    .post()
                .then()
                        .statusCode(201);
    }

    @Test
    @DisplayName("should return 404 when try to create a post for user that doesn't exist")
    public void postForInexistentUserTest() {

        var postRequest = new CreatePostRequest();
        postRequest.setText("Hello World Dri");

        var inexistentUserId = 999;

        var response = given()
                    .contentType(ContentType.JSON)
                    .body(postRequest)
                    .pathParams("userId", inexistentUserId)
                .when()
                    .post()
                .then()
                    .statusCode(404);
    }
    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest(){
        var inexistentUserId = 999;

        given()
             .pathParams("userId", inexistentUserId)
          .when()
             .get()
          .then()
             .statusCode(404);
    }

    @Test
    @DisplayName("should return 400 when followerId header is not present")
    public void listPostsFollowerHeaderNotSendTest(){

        given()
             .pathParams("userId", userId)
          .when()
             .get()
          .then()
             .statusCode(400)
             .body(Matchers.is("You forgot to pass the followerId on Header"));
    }

    @Test
    @DisplayName("should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest(){

        var inexistentFollowerId = 999;

        given()
             .pathParams("userId", userId)
             .header("followerId", inexistentFollowerId)
          .when()
             .get()
          .then()
             .statusCode(400)
             .body(Matchers.is("FollowerId doesn't exist"));
    }

    @Test
    @DisplayName("should return 403 when follower isn't a follower")
    public void listPostNotAFollowerTest(){

        given()
              .pathParams("userId", userId)
              .header("followerId", userNotFollowerId)
         .when()
              .get()
         .then()
              .statusCode(403)
              .body(Matchers.is("You can't see these posts, follow the user to see"));
    }

    @Test
    @DisplayName("should return posts")
    public void listPostTest(){
        given()
            .pathParams("userId", userId)
            .header("followerId", userFollowerId)
          .when()
            .get()
         .then()
            .statusCode(200)
            .body("size()", Matchers.is(1));
    }
}