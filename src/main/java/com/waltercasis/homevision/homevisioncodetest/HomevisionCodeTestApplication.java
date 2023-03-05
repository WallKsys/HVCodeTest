package com.waltercasis.homevision.homevisioncodetest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waltercasis.homevision.homevisioncodetest.model.House;
import com.waltercasis.homevision.homevisioncodetest.model.response.HouseResponse;

public class HomevisionCodeTestApplication {

	private static final String API_ENDPOINT = "http://app-homevision-staging.herokuapp.com/api_project/houses";
	private static final int NUM_THREADS = 5;
	private static final String IMAGE_DIR = "images/";

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final int MAX_RETRIES = 5;
	private static final int RETRY_DELAY_MS = 500;


	public static void main(String[] args) throws IOException {
		List<House> allHouses = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

		for (int page = 1; page <= 10; page++) {
			List<House> houses = null;
			try {
				houses = fetchHouses(page);
			} catch (RuntimeException e) {
				System.err.println("Error fetching houses from API (page " + page + "), skipping page: " + e.getMessage());
				continue;
			}

			allHouses.addAll(houses);

			for (House house : houses) {
				executorService.execute(() -> {
					try {
						downloadImage(house);
					} catch (IOException e) {
						System.err.println("Error downloading image for house " + house.getId() + ": " + e.getMessage());
					}
				});
			}

			executorService.shutdown();
			while (!executorService.isTerminated()) {
				// Wait for all tasks to finish
			}

			System.out.println("All houses downloaded successfully");


		}
	}

	protected static List<House> fetchHouses(int page) throws IOException {
		String url = API_ENDPOINT + "?page=" + page;
		JsonNode responseNode = null;
		int retries = 0;
		while (retries < MAX_RETRIES) {
			try {
				responseNode = objectMapper.readTree(new URL(url));
				break;
			} catch (IOException e) {
				System.err.println("Error fetching houses from API (page " + page + "), retrying in " + RETRY_DELAY_MS + " ms: " + e.getMessage());
				retries++;
				try {
					Thread.sleep(RETRY_DELAY_MS);
				} catch (InterruptedException ie) {
					System.err.println("Interrupted while waiting for retry: " + ie.getMessage());
				}
			}
		}

		if (responseNode != null && responseNode.has("ok") && responseNode.get("ok").asBoolean()) {
			List<House> houses = new ArrayList<>();
			JsonNode housesNode = responseNode.get("houses");
			if (housesNode.isArray() && housesNode.size() > 0) {
				for (JsonNode houseNode : housesNode) {
					HouseResponse house = objectMapper.treeToValue(houseNode, HouseResponse.class);
					houses.add(new House(house.getId(), house.getAddress(), house.getHomeowner(), house.getPrice(),
							house.getPhotoUrl()));
				}
				return houses;
			} else {
				throw new RuntimeException("API Error: empty houses response");
			}
		} else {
			throw new RuntimeException("API Error: " + (responseNode != null ? responseNode.get("error").asText() : "null response"));
		}
	}



	private static void downloadImage(House house) throws IOException {
			String imageUrl = house.getPhotoUrl();
			File imagesFolder = new File("images");
			if (!imagesFolder.exists()) {
				imagesFolder.mkdir();
			}
			String imageFilename = IMAGE_DIR + house.getId() + "-" + house.getAddress().replaceAll("\\W+", "") + ".jpg";

			URL url = new URL(imageUrl);
			InputStream inputStream = url.openStream();
			FileOutputStream outputStream = new FileOutputStream(new File(imageFilename));

			byte[] buffer = new byte[2048];
			int length;

			while ((length = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}

			inputStream.close();
			outputStream.close();
			System.out.println("Image downloaded for house " + house.getId());
		}



}