package ru.yandex.praktikum.api.clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.yandex.praktikum.api.models.UserModel;

import static io.restassured.RestAssured.given;

public class UserClient {
    private static final String BASE_URL = "https://stellarburgers.education-services.ru";
    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String USER_PATH = "/api/auth/user";

    @Step("Создание пользователя через API")
    public Response createUser(UserModel user) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(user)
                .post(REGISTER_PATH);
    }

    @Step("Логин пользователя через API")
    public Response loginUser(UserModel user) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(user)
                .post(LOGIN_PATH);
    }

    @Step("Удаление пользователя через API")
    public Response deleteUser(String accessToken) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .delete(USER_PATH);
    }
}