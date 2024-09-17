package com.example.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class EmployeeApiTests {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String AUTH_USERNAME = "stella";
    private static final String AUTH_PASSWORD = "sun-fairy";
    private static String authToken;

    @BeforeAll
    public static void setUp() {
        // Авторизация и получение токена
        String authRequestBody = "{ \"username\": \"" + AUTH_USERNAME + "\", \"password\": \"" + AUTH_PASSWORD + "\" }";

        Response authResponse = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/auth/login")
                .contentType("application/json")
                .body(authRequestBody)
                .post();

        authResponse.then().statusCode(201);
        authToken = authResponse.jsonPath().getString("userToken");
    }

    // Позитивные тесты

    @Test
    public void testLogin() {
        String requestBody = "{ \"username\": \"" + AUTH_USERNAME + "\", \"password\": \"" + AUTH_PASSWORD + "\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/auth/login")
                .contentType("application/json")
                .body(requestBody)
                .post();

        response.then()
                .statusCode(201)
                .body("userToken", equalTo(authToken))
                .body("role", equalTo("client"))
                .body("displayName", equalTo("Test User"))
                .body("login", equalTo(AUTH_USERNAME));
    }

    @Test
    public void testAddCompany() {
        String requestBody = "{ \"name\": \"New Company\", \"description\": \"Company Description\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company")
                .header("x-client-token", authToken)
                .contentType("application/json")
                .body(requestBody)
                .post();

        response.then()
                .statusCode(201)
                .body("id", equalTo(1)); // Замените на ожидаемый ID
    }

    @Test
    public void testGetCompanyById() {
        int companyId = 1;

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company/" + companyId)
                .get();

        response.then()
                .statusCode(200)
                .body("id", equalTo(companyId))
                .body("name", equalTo("New Company"))
                .body("description", equalTo("Company Description"))
                .body("isActive", equalTo(true));
    }

    @Test
    public void testUpdateCompany() {
        int companyId = 1;
        String requestBody = "{ \"name\": \"Updated Company\", \"description\": \"Updated Description\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company/" + companyId)
                .header("x-client-token", authToken)
                .contentType("application/json")
                .body(requestBody)
                .patch();

        response.then()
                .statusCode(202)
                .body("id", equalTo(companyId))
                .body("name", equalTo("Updated Company"))
                .body("description", equalTo("Updated Description"));
    }

    @Test
    public void testDeleteCompany() {
        int companyId = 1;

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company/delete/" + companyId)
                .header("x-client-token", authToken)
                .get();

        response.then()
                .statusCode(200)
                .body("id", equalTo(companyId))
                .body("name", equalTo("Updated Company"))
                .body("description", equalTo("Updated Description"))
                .body("isActive", equalTo(false)); // предполагаем, что компания была удалена и статус стал false
    }

    @Test
    public void testSetCompanyStatus() {
        int companyId = 1;
        String requestBody = "{ \"isActive\": false }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company/status/" + companyId)
                .header("x-client-token", authToken)
                .contentType("application/json")
                .body(requestBody)
                .patch();

        response.then()
                .statusCode(201)
                .body("id", equalTo(companyId))
                .body("isActive", equalTo(false));
    }

    @Test
    public void testGetEmployees() {
        int companyId = 1;

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/employee")
                .queryParam("company", companyId)
                .get();

        response.then()
                .statusCode(200)
                .body("", hasSize(5)) // Предположим, что у компании 5 сотрудников
                .body("[0].companyId", equalTo(companyId));
    }

    @Test
    public void testAddEmployee() {
        String requestBody = "{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"companyId\": 1, \"email\": \"john.doe@example.com\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/employee")
                .header("x-client-token", authToken)
                .contentType("application/json")
                .body(requestBody)
                .post();

        response.then()
                .statusCode(201)
                .body("id", equalTo(1)); // Замените на ожидаемый ID
    }

    @Test
    public void testGetEmployeeById() {
        int employeeId = 1;

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/employee/" + employeeId)
                .get();

        response.then()
                .statusCode(200)
                .body("id", equalTo(employeeId))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("email", equalTo("john.doe@example.com"));
    }

    @Test
    public void testUpdateEmployee() {
        int employeeId = 1;
        String requestBody = "{ \"lastName\": \"Smith\", \"email\": \"john.smith@example.com\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/employee/" + employeeId)
                .header("x-client-token", authToken)
                .contentType("application/json")
                .body(requestBody)
                .patch();

        response.then()
                .statusCode(201)
                .body("id", equalTo(employeeId))
                .body("lastName", equalTo("Smith"))
                .body("email", equalTo("john.smith@example.com"));
    }

    // Негативные тесты

    @Test
    public void testAddCompanyUnauthorized() {
        String requestBody = "{ \"name\": \"Unauthorized Company\", \"description\": \"Description\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company")
                .contentType("application/json")
                .body(requestBody)
                .post();

        response.then()
                .statusCode(401); // Ожидаем статус 401 Unauthorized
    }

    @Test
    public void testGetCompanyByIdNotFound() {
        int companyId = 9999; // Не существующий ID

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company/" + companyId)
                .get();

        response.then()
                .statusCode(404); // Ожидаем статус 404 Not Found
    }

    @Test
    public void testUpdateCompanyUnauthorized() {
        int companyId = 1;
        String requestBody = "{ \"name\": \"Unauthorized Update\", \"description\": \"Updated Description\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company/" + companyId)
                .contentType("application/json")
                .body(requestBody)
                .patch();

        response.then()
                .statusCode(401); // Ожидаем статус 401 Unauthorized
    }

    @Test
    public void testDeleteCompanyUnauthorized() {
        int companyId = 1;

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company/delete/" + companyId)
                .contentType("application/json")
                .get();

        response.then()
                .statusCode(401); // Ожидаем статус 401 Unauthorized
    }

    @Test
    public void testSetCompanyStatusUnauthorized() {
        int companyId = 1;
        String requestBody = "{ \"isActive\": false }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/company/status/" + companyId)
                .contentType("application/json")
                .body(requestBody)
                .patch();

        response.then()
                .statusCode(401); // Ожидаем статус 401 Unauthorized
    }

    @Test
    public void testGetEmployeesUnauthorized() {
        int companyId = 1;

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/employee")
                .queryParam("company", companyId)
                .get();

        response.then()
                .statusCode(401); // Ожидаем статус 401 Unauthorized
    }

    @Test
    public void testAddEmployeeUnauthorized() {
        String requestBody = "{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"companyId\": 1, \"email\": \"john.doe@example.com\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/employee")
                .contentType("application/json")
                .body(requestBody)
                .post();

        response.then()
                .statusCode(401); // Ожидаем статус 401 Unauthorized
    }

    @Test
    public void testGetEmployeeByIdNotFound() {
        int employeeId = 9999; // Не существующий ID

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/employee/" + employeeId)
                .get();

        response.then()
                .statusCode(404); // Ожидаем статус 404 Not Found
    }

    @Test
    public void testUpdateEmployeeUnauthorized() {
        int employeeId = 1;
        String requestBody = "{ \"lastName\": \"Unauthorized\", \"email\": \"unauthorized@example.com\" }";

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .basePath("/employee/" + employeeId)
                .contentType("application/json")
                .body(requestBody)
                .patch();

        response.then()
                .statusCode(401); // Ожидаем статус 401 Unauthorized
    }
}