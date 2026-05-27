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

public class CreateUserTest {

    private UserClient userClient;
    private UserModel user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
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
    @DisplayName("Создание уникального пользователя")
    @Description("Позитивный сценарий регистрации нового пользователя с валидными email, паролем и именем. Ожидается статус 200, наличие токена и корректные данные в ответе.")
    public void createUniqueUserTest() {
        var response = userClient.createUser(user);
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", notNullValue());
        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Создание уже существующего пользователя")
    @Description("Негативный сценарий: попытка зарегистрировать пользователя с уже существующими данными. Ожидается статус 403 и сообщение 'User already exists'.")
    public void createExistingUserTest() {
        userClient.createUser(user).then().statusCode(HttpStatus.SC_OK);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
        var loginResp = userClient.loginUser(user);
        accessToken = loginResp.then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Негативный сценарий: отсутствует обязательное поле 'password'. Ожидается статус 403 и сообщение 'Email, password and name are required fields'.")
    public void createUserWithoutPasswordTest() {
        user.setPassword(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
        accessToken = null; // пользователь не создан
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Негативный сценарий: отсутствует поле 'name'. Ожидается статус 403 и соответствующее сообщение.")
    public void createUserWithoutNameTest() {
        user.setName(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    @Description("Негативный сценарий: отсутствует поле 'email'. Ожидается статус 403 и сообщение об обязательных полях.")
    public void createUserWithoutEmailTest() {
        user.setEmail(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("message", equalTo("Email, password and name are required fields"));
    }
}