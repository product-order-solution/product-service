package com.techie.microservices.product;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;

// The @Import annotation explicitly includes the TestcontainersConfiguration class in the test, making the MongoDBContainer available as a bean.
// @SpringBootTest annotation tells Spring Boot to start the application context, including the embedded MongoDB container, which is registered
// as a bean via TestcontainersConfiguration.
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {
	// This is an injected Spring bean that allows interaction with the MongoDB instance (which is actually the mongoDbContainer).
	@Autowired
	private MongoTemplate mongoTemplate; // Spring MongoDB support

	//When the test run, the port chosen will be added to this variable.
	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		// Configuration for RestAssured.
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	@Test
	void testMongoContainerIsRunning() {
		// Assert that the MongoDB container is running and MongoTemplate can interact with it
		assertThat(mongoTemplate.getDb().getName()).isNotNull();
	}


	@Test
	@Order(1)
	void shouldCreateProduct() {
		String requestBody = """
			{
				"name": "iPhone 15",
				"description": "iPhone 15 is a smart phone from Apple",
				"price": 1000
			}""";

		RestAssured.given()
				.header("Content-Type", "application/json")
				.body(requestBody)
				.when()
				.post("/api/product")
				.then()
				.statusCode(201)
				.body("id", org.hamcrest.Matchers.notNullValue())
				.body("name", org.hamcrest.Matchers.equalTo("iPhone 15"))
				.body("description", org.hamcrest.Matchers.equalTo("iPhone 15 is a smart phone from Apple"))
				.body("price", org.hamcrest.Matchers.equalTo(1000));
	}

	@Test
	@Order(2)
	void shouldGetAllProducts() {
		RestAssured.given()
				.when()
				.get("/api/product")
				.then()
				.statusCode(200)
				.body("size()", org.hamcrest.Matchers.equalTo(1))
				.body("[0].id", org.hamcrest.Matchers.notNullValue())
				.body("[0].name", org.hamcrest.Matchers.equalTo("iPhone 15"))
				.body("[0].description", org.hamcrest.Matchers.equalTo("iPhone 15 is a smart phone from Apple"))
				.body("[0].price", org.hamcrest.Matchers.equalTo(1000));
	}

}
