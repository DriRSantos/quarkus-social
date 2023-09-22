package com.github.drirsantos.quarkussocial.rest;

import com.github.drirsantos.quarkussocial.domain.model.Follower;
import com.github.drirsantos.quarkussocial.domain.model.User;
import com.github.drirsantos.quarkussocial.domain.repository.FollowerRepository;
import com.github.drirsantos.quarkussocial.domain.repository.PostRepository;
import com.github.drirsantos.quarkussocial.domain.repository.UserRepository;
import com.github.drirsantos.quarkussocial.rest.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {
        var user = new User();
        user.setAge(14);
        user.setName("Dri");
        userRepository.persist(user);
        userId = user.getId();

        var follower = new User();
        follower.setAge(14);
        follower.setName("Dri");
        userRepository.persist(follower);
        followerId = follower.getId();

        Follower followerEntity = new Follower();
        followerEntity.setUser(user);
        followerEntity.setFollower(follower);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("should return 409 when Follower Id is equal User Id")
    public void sameUserAsFollowerTest(){

        var body = new FollowerRequest();
        body.setFollowerId(userId);

            given()
                 .contentType(ContentType.JSON)
                 .body(body)
                 .pathParams("userId", userId)
              .when()
                 .put()
             .then()
                 .statusCode(Response.Status.CONFLICT.getStatusCode())
                 .body(Matchers.is("You can't follow yourself"));
    }

    @Test
    @DisplayName("should return 404 try to follow a user that doesn't exist")
    public void userNotFoundWhenTryToFollowTest(){

        var inexistentUserId = 999;
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParams("userId", inexistentUserId)
            .when()
                .put()
            .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should follow a user")
    public void followUserTest(){

        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParams("userId", userId)
            .when()
                .put()
           .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("should return 404 on list followers and userId doesn't exist")
    public void userNotFoundWhenListFollowersTest(){

        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParams("userId", inexistentUserId)
            .when()
                .get()
           .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should list a user's followers")
    public void listFollowersTest(){

        var response = given()
                            .contentType(ContentType.JSON)
                            .pathParams("userId", userId)
                        .when()
                            .get()
                        .then()
                            .extract().response();

        var followersCount = response.jsonPath().get("followersCount");
        var followersContent = response.jsonPath().getList("content");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());
    }

    @Test
    @DisplayName("should return 404 on unfollow user and userId doesn't exist")
    public void userNotFoundAndUnfollowAUserTest(){

        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParams("userId", inexistentUserId)
                .queryParam("followerId", followerId)
           .when()
                .delete()
          .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should unfollow a user")
    public void unFollowUserTest(){
        given()
                .pathParams("userId", userId)
                .queryParam("followerId", followerId)
            .when()
                .delete()
            .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

}