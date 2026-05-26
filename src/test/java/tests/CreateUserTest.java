package api.tests;

import api.clients.UserClient;
import api.generators.UserGenerator;
import api.models.UserModel;
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
        user = UserGenerator.getRandomUser();
    }

    @After
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Позитивный сценарий: регистрация с новыми email, password, name")
    public void createUniqueUserTest() {
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", notNullValue());
        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Создание уже существующего пользователя")
    @Description("Негативный сценарий: повторная регистрация с теми же данными")
    public void createExistingUserTest() {
        userClient.createUser(user).then().statusCode(SC_OK);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
        // получаем токен первого пользователя для удаления
        var loginResp = userClient.loginUser(user);
        accessToken = loginResp.then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Негативный сценарий: отсутствует обязательное поле password")
    public void createUserWithoutPasswordTest() {
        user.setPassword(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
        accessToken = null;
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    public void createUserWithoutNameTest() {
        user.setName(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    public void createUserWithoutEmailTest() {
        user.setEmail(null);
        var response = userClient.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("message", equalTo("Email, password and name are required fields"));
    }
}