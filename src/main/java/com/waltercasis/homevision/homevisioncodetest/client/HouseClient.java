package com.waltercasis.homevision.homevisioncodetest.client;

import com.waltercasis.homevision.homevisioncodetest.model.response.HouseResponse;
import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadGateway;
import org.springframework.web.reactive.function.client.WebClientResponseException.GatewayTimeout;
import org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetryBackoffSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
public class HouseClient {

    String API_ENDPOINT = "http://app-homevision-staging.herokuapp.com/api_project";

    /**
     * Retrieves a list of houses from a remote API endpoint.
     *
     * @param page the page number to retrieve
     * @return a Mono emitting a HousesApiResponse object representing the API response
     */
    public Mono<HousesApiResponse> getHouses(int page) {
        return WebClient.create(API_ENDPOINT) // Create a new WebClient instance targeting the API endpoint
                .get() // Issue a GET request
                .uri(uriBuilder -> uriBuilder // Build the request URI using a UriBuilder
                        .path("/houses") // Set the path to "/houses"
                        .queryParam("page", page) // Add a query parameter for the page number
                        .build())
                .retrieve() // Retrieve the response
                .bodyToMono(HousesApiResponse.class)// Deserialize the response body into a HousesApiResponse object
                .log() // Log the response
                .retryWhen(RetryBackoffSpec // Retry the request with exponential backoff if it fails with certain exceptions
                        .backoff(2, Duration.ofMillis(100))
                        .filter(t -> t instanceof BadGateway ||
                                t instanceof GatewayTimeout ||
                                t instanceof ServiceUnavailable));
    }

    public Mono<String> downloadAndSavePhoto(HouseResponse house) {

        // Get the photo URL, ID, and address from the HouseResponse object
        String photoUrl = house.getPhotoUrl();
        String id = house.getId();
        String address = house.getAddress();

        // Extract the file extension from the photo URL
        String extension = photoUrl.substring(photoUrl.lastIndexOf(".") + 1);

        // Create a file name using the ID, address, and extension
        String fileName = id + "-" + address + "." + extension;

        // Specify the directory path where the photo will be saved
        String directoryPath = "imgs";
        String filePath = directoryPath + File.separator + fileName;

        // Create a directory if it doesn't already exist
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Check if the file already exists
        File file = new File(filePath);
        if (file.exists()) {
            log.info("Skipping download, file already exists: " + filePath);
            return Mono.just(fileName);
        }

        // Use a WebClient to download the photo as a byte array
        return WebClient.create()
                .get()
                .uri(photoUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .map(bytes -> {
                    // Write the byte array to a file
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        log.info("saving the file: " + filePath);
                        fos.write(bytes);
                    } catch (IOException e) {
                        // Throw a runtime exception if there's an error writing the file
                        throw new RuntimeException("Error writing file: " + filePath, e);
                    }
                    return fileName;
                })
                // Use the retryWhen operator to retry the download up to two times if there's a specific error
                // (BadGateway, GatewayTimeout, or ServiceUnavailable)
                .retryWhen(RetryBackoffSpec
                        .backoff(2, Duration.ofMillis(100))
                        .filter(t -> t instanceof BadGateway ||
                                t instanceof GatewayTimeout ||
                                t instanceof ServiceUnavailable));
    }



}
