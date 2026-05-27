package ru.yandex.praktikum.api.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.praktikum.api.clients.OrderClient;
import ru.yandex.praktikum.api.clients.UserClient;
import ru.yandex.praktikum.api.generators.UserGenerator;
import ru.yandex.praktikum.api.models.OrderModel;
import ru.yandex.praktikum.api.models.UserModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class CreateOrderTest {

    private OrderClient orderClient;
    private UserClient userClient;
    private UserModel user;
    private String accessToken;
    private List<String> validIngredientIds;

    @Before
    public void setUp() {
        orderClient = new OrderClient();
        userClient = new UserClient();

        // Получаем актуальные ID ингредиентов
        var ingredientResponse = orderClient.getIngredients();
        ingredientResponse.then().statusCode(HttpStatus.SC_OK);
        validIngredientIds = ingredientResponse.then().extract().path("data._id");
        if (validIngredientIds == null || validIngredientIds.size() < 2) {
            throw new IllegalStateException("Недостаточно ингредиентов для теста");
        }

        user = UserGenerator.getRandomUser();
        userClient.createUser(user).then().statusCode(HttpStatus.SC_OK);
        var loginResp = userClient.loginUser(user);
        accessToken = loginResp.then().extract().path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Позитивный сценарий: авторизованный пользователь создаёт заказ с валидными ID ингредиентов. Ожидается статус 200, success: true, номер заказа не null.")
    public void createOrderWithAuthAndIngredientsTest() {
        OrderModel order = new OrderModel(Arrays.asList(
                validIngredientIds.get(0),
                validIngredientIds.get(1)
        ));
        var response = orderClient.createOrderWithAuth(order, accessToken);
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации, но с ингредиентами")
    @Description("Фактическое поведение API: заказ создаётся даже без токена авторизации. Ожидается 200 и корректные данные в ответе.")
    public void createOrderWithoutAuthButWithIngredientsTest() {
        OrderModel order = new OrderModel(Collections.singletonList(validIngredientIds.get(0)));
        var response = orderClient.createOrderWithoutAuth(order);
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией, но без ингредиентов")
    @Description("Негативный сценарий: пустой список ингредиентов. Ожидается статус 400 и сообщение 'Ingredient ids must be provided'.")
    public void createOrderWithAuthAndNoIngredientsTest() {
        OrderModel order = new OrderModel(Collections.emptyList());
        var response = orderClient.createOrderWithAuth(order, accessToken);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиента")
    @Description("Негативный сценарий: передан невалидный ID ингредиента. Ожидается статус 500 Internal Server Error.")
    public void createOrderWithInvalidIngredientHashTest() {
        OrderModel order = new OrderModel(Collections.singletonList("invalid_hash_123"));
        var response = orderClient.createOrderWithAuth(order, accessToken);
        response.then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}