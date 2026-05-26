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
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        } else {
            // если тест логина не сохранил токен, попробуем удалить через логин
            var resp = userClient.loginUser(user);
            if (resp.statusCode() == SC_OK) {
                String token = resp.then().extract().path("accessToken");
                userClient.deleteUser(token);
            }
        }
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Позитивный сценарий: логин с валидными email/password")
    public void loginWithValidUserTest() {
        var response = userClient.loginUser(user);
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()));
        accessToken = response.then().extract().path("accessToken");
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
    public void loginWithWrongPasswordTest() {
        UserModel wrongUser = new UserModel(user.getEmail(), "wrongPass", null);
        var response = userClient.loginUser(wrongUser);
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с пустыми полями")
    public void loginWithEmptyFieldsTest() {
        UserModel emptyUser = new UserModel("", "", null);
        var response = userClient.loginUser(emptyUser);
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("message", equalTo("email or password are incorrect"));
    }
}
