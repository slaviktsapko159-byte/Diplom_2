package tests;

import clients.UserClient;
import generators.UserGenerator;
import models.UserModel;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class LoginUserTest {
    private UserClient userClient;
    private UserModel user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
        userClient.createUser(user).then().statusCode(SC_OK);
    }

    @After
    public void tearDown() {
        if (user != null) {
            var loginResp = userClient.loginUser(user);
            if (loginResp.statusCode() == SC_OK) {
                accessToken = loginResp.then().extract().path("accessToken");
                userClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Авторизация с валидными email и паролем")
    public void loginWithValidUserTest() {
        var response = userClient.loginUser(user);
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()));
    }

    @Test
    @DisplayName("Вход с неверным email")
    @Description("Негативный сценарий: неверный логин")
    public void loginWithWrongEmailTest() {
        UserModel wrongUser = new UserModel("wrong@test.ru", user.getPassword(), null);
        var response = userClient.loginUser(wrongUser);
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Негативный сценарий: неверный пароль")
    public void loginWithWrongPasswordTest() {
        UserModel wrongUser = new UserModel(user.getEmail(), "wrongPass", null);
        var response = userClient.loginUser(wrongUser);
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с пустыми полями")
    @Description("Негативный сценарий: пустые email и пароль")
    public void loginWithEmptyFieldsTest() {
        UserModel emptyUser = new UserModel("", "", null);
        var response = userClient.loginUser(emptyUser);
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("message", equalTo("email or password are incorrect"));
    }
}