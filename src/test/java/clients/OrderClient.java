package ru.yandex.praktikum.api.clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.yandex.praktikum.api.models.OrderModel;

import static io.restassured.RestAssured.given;

public class OrderClient {
    private static final String BASE_URL = "https://stellarburgers.education-services.ru";
    private static final String ORDERS_PATH = "/api/orders";
    private static final String INGREDIENTS_PATH = "/api/ingredients";

    @Step("Получение списка ингредиентов")
    public Response getIngredients() {
        return given()
                .baseUri(BASE_URL)
                .get(INGREDIENTS_PATH);
    }

    @Step("Создание заказа с авторизацией")
    public Response createOrderWithAuth(OrderModel order, String accessToken) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .body(order)
                .post(ORDERS_PATH);
    }

    @Step("Создание заказа без авторизации")
    public Response createOrderWithoutAuth(OrderModel order) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(order)
                .post(ORDERS_PATH);
    }
}