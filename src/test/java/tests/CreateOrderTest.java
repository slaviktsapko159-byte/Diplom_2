package tests;

import clients.OrderClient;
import clients.UserClient;
import generators.UserGenerator;
import models.OrderModel;
import models.UserModel;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
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

        var ingredientResponse = orderClient.getIngredients();
        ingredientResponse.then().statusCode(SC_OK);
        validIngredientIds = ingredientResponse.then().extract().path("data._id");
        if (validIngredientIds == null || validIngredientIds.size() < 2) {
            throw new IllegalStateException("Недостаточно ингредиентов для теста");
        }

        user = UserGenerator.getRandomUser();
        userClient.createUser(user).then().statusCode(SC_OK);
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
    @Description("Позитивный сценарий: авторизованный пользователь создаёт заказ с валидными ингредиентами")
    public void createOrderWithAuthAndIngredientsTest() {
        OrderModel order = new OrderModel(Arrays.asList(
                validIngredientIds.get(0),
                validIngredientIds.get(1)
        ));
        var response = orderClient.createOrderWithAuth(order, accessToken);
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации, но с ингредиентами")
    @Description("Проверка, что неавторизованный пользователь может создать заказ (фактическое поведение API)")
    public void createOrderWithoutAuthButWithIngredientsTest() {
        OrderModel order = new OrderModel(Collections.singletonList(validIngredientIds.get(0)));
        var response = orderClient.createOrderWithoutAuth(order);
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией, но без ингредиентов")
    @Description("Негативный сценарий: пустой список ингредиентов -> 400 Bad Request")
    public void createOrderWithAuthAndNoIngredientsTest() {
        OrderModel order = new OrderModel(Collections.emptyList());
        var response = orderClient.createOrderWithAuth(order, accessToken);
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиента")
    @Description("Негативный сценарий: невалидный ID ингредиента -> 500 Internal Server Error")
    public void createOrderWithInvalidIngredientHashTest() {
        OrderModel order = new OrderModel(Collections.singletonList("invalid_hash_123"));
        var response = orderClient.createOrderWithAuth(order, accessToken);
        response.then().statusCode(SC_INTERNAL_SERVER_ERROR);
    }
}