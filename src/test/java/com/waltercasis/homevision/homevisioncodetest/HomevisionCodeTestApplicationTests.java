package com.waltercasis.homevision.homevisioncodetest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waltercasis.homevision.homevisioncodetest.model.House;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static com.waltercasis.homevision.homevisioncodetest.HomevisionCodeTestApplication.fetchHouses;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HomevisionCodeTestApplication.class)
public class HomevisionCodeTestApplicationTests {

	public HomevisionCodeTestApplicationTests() {
	}

	@Test
	public void testFetchHousesHappyPath() throws IOException {
		// create a mock response with valid JSON data
		MockResponse mockResponse = new MockResponse()
				.setResponseCode(200)
				.setBody("{\n" +
						"    \"ok\": true,\n" +
						"    \"houses\": [\n" +
						"        {\n" +
						"            \"id\": 1,\n" +
						"            \"address\": \"123 Main St\",\n" +
						"            \"homeowner\": \"John Doe\",\n" +
						"            \"price\": 100000,\n" +
						"            \"photoUrl\": \"https://example.com/photo.jpg\"\n" +
						"        },\n" +
						"        {\n" +
						"            \"id\": 2,\n" +
						"            \"address\": \"456 Oak St\",\n" +
						"            \"homeowner\": \"Jane Smith\",\n" +
						"            \"price\": 200000,\n" +
						"            \"photoUrl\": \"https://example.com/photo2.jpg\"\n" +
						"        }\n" +
						"    ]\n" +
						"}");

		// start the mock server and enqueue the mock response
		MockWebServer server = new MockWebServer();
		server.enqueue(mockResponse);
		server.start();

		// set the API endpoint URL to the mock server's URL
		String apiUrl = server.url("http://app-homevision-staging.herokuapp.com/api_project/houses").toString();
		String API_ENDPOINT = apiUrl;

		// make the API call
		List<House> houses = fetchHouses(1);

		// verify that the API call succeeded and returned the expected data
		assertThat(houses.size(), equalTo(2));
		assertThat(houses.get(0).getId(), equalTo(1));
		assertThat(houses.get(0).getAddress(), equalTo("123 Main St"));
		assertThat(houses.get(0).getHomeowner(), equalTo("John Doe"));
		assertThat(houses.get(0).getPrice(), equalTo(100000));
		assertThat(houses.get(0).getPhotoUrl(), equalTo("https://example.com/photo.jpg"));
		assertThat(houses.get(1).getId(), equalTo(2));
		assertThat(houses.get(1).getAddress(), equalTo("456 Oak St"));
		assertThat(houses.get(1).getHomeowner(), equalTo("Jane Smith"));
		assertThat(houses.get(1).getPrice(), equalTo(200000));
		assertThat(houses.get(1).getPhotoUrl(), equalTo("https://example.com/photo2.jpg"));

		// shut down the mock server
		server.shutdown();
	}
	@Test
	public void testFetchHousesNullResponse() {
		Assertions.assertThrows(RuntimeException.class, () -> {
			List<House> houses = fetchHouses(0);
		});
	}

	@Test
	public void testFetchHousesNoOkField() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode responseNode = mapper.createObjectNode();
		ArrayNode housesNode = mapper.createArrayNode();
		housesNode.add(mapper.createObjectNode().put("id", 1).put("address", "123 Main St"));
		housesNode.add(mapper.createObjectNode().put("id", 2).put("address", "456 Oak Ave"));
		responseNode.set("houses", housesNode);
		String response = mapper.writeValueAsString(responseNode);

		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setResponseCode(200).setBody(response));
		server.start();

		String url = server.url("/").toString() + "?page=0";

		Assertions.assertThrows(RuntimeException.class, () -> {
			List<House> houses = fetchHouses(0);
		});

		server.shutdown();
	}

	@Test
	public void testFetchHousesNotOk() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put("ok", false);
		responseNode.put("error", "Invalid API key");
		String response = mapper.writeValueAsString(responseNode);

		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setResponseCode(200).setBody(response));
		server.start();

		String url = server.url("/").toString() + "?page=1";

		Assertions.assertThrows(RuntimeException.class, () -> {
			List<House> houses = fetchHouses(1);
		});

		server.shutdown();
	}

	@Test
	public void testFetchHousesInvalidResponse() throws Exception {
		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setResponseCode(200).setBody("{invalid-json"));
		server.start();

		String url = server.url("/").toString() + "?page=0";

		Assertions.assertThrows(RuntimeException.class, () -> {
			List<House> houses = fetchHouses(0);
		});

		server.shutdown();
	}

	@Test
	public void testFetchHousesApiError() throws Exception {
		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
		server.start();

		String url = server.url("/").toString() + "?page=0";

		Assertions.assertThrows(RuntimeException.class, () -> {
			List<House> houses = fetchHouses(0);
		});

		server.shutdown();
	}



}
