package ru.yandex.praktikum.api.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.praktikum.api.clients.UserClient;
import ru.yandex.praktikum.api.generators.UserGenerator;
import ru.yandex.praktikum.api.models.UserModel;

import static org.hamcrest.Matchers.*;

public class LoginUserTest {

    private UserClient userClient;
    private UserModel user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
        userClient.createUser(user).then().statusCode(HttpStatus.SC_OK);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        } else if (user != null && user.getPassword() != null) {
            var loginResp = userClient.loginUser(user);
            if (loginResp.statusCode() == HttpStatus.SC_OK) {
                accessToken = loginResp.then().extract().path("accessToken");
                userClient.deleteUser(accessToken);
            }
        }
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Позитивный сценарий: авторизация с корректными email и паролем. Ожидается статус 200, accessToken не пустой, возвращаются данные пользователя.")
    public void loginWithValidUserTest() {
        var response = userClient.loginUser(user);
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()));
        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Вход с неверным email")
    @Description("Негативный сценарий: указан неверный email. Ожидается статус 401 и сообщение 'email or password are incorrect'.")
    public void loginWithWrongEmailTest() {
        UserModel wrongUser = new UserModel("wrong@test.ru", user.getPassword(), null);
        var response = userClient.loginUser(wrongUser);
        response.then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Негативный сценарий: указан неверный пароль. Ожидается статус 401 и сообщение 'email or password are incorrect'.")
    public void loginWithWrongPasswordTest() {
        UserModel wrongUser = new UserModel(user.getEmail(), "wrongPass", null);
        var response = userClient.loginUser(wrongUser);
        response.then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с пустыми полями")
    @Description("Негативный сценарий: поля email и пароль пустые. Ожидается статус 401 и сообщение 'email or password are incorrect'.")
    public void loginWithEmptyFieldsTest() {
        UserModel emptyUser = new UserModel("", "", null);
        var response = userClient.loginUser(emptyUser);
        response.then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("message", equalTo("email or password are incorrect"));
    }
}