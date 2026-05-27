package tests;

import clients.UserClient;
import models.UserModel;
import generators.UserGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class CreateUserTest {
    private UserClient userClient;
    private UserModel user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
    }

    @After
    public void tearDown() {
        if (user != null && user.getEmail() != null && user.getPassword() != null) {
            var loginResp = userClient.loginUser(user);
            if (loginResp.statusCode() == SC_OK) {
                accessToken = loginResp.then().extract().path("accessToken");
                userClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Позитивный сценарий: регистрация с новыми данными")
    public void createUniqueUserTest() {
        user = UserGenerator.getRandomUser();
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Создание уже существующего пользователя")
    @Description("Негативный сценарий: повторная регистрация")
    public void createExistingUserTest() {
        user = UserGenerator.getRandomUser();
        userClient.createUser(user).then().statusCode(SC_OK);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    public void createUserWithoutPasswordTest() {
        user = UserGenerator.getRandomUser();
        user.setPassword(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    public void createUserWithoutNameTest() {
        user = UserGenerator.getRandomUser();
        user.setName(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    public void createUserWithoutEmailTest() {
        user = UserGenerator.getRandomUser();
        user.setEmail(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("message", equalTo("Email, password and name are required fields"));
    }
}