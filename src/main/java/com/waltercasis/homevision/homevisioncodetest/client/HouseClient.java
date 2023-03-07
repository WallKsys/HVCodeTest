package com.waltercasis.homevision.homevisioncodetest.client;

import com.waltercasis.homevision.homevisioncodetest.model.response.HouseResponse;
import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import com.waltercasis.homevision.homevisioncodetest.utils.PhotoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetryBackoffSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
public class HouseClient {


    @Value("${api.homevision.endpoint}")
    String apiEndpoint;

    @Value("${api.photo.folder}")
    String photoFolder;
    private PhotoUtils photoUtils;

    public HouseClient(PhotoUtils photoUtils) {
        this.photoUtils = photoUtils;
    }






    /**
     * Retrieves a list of houses from a remote API endpoint.
     *
     * @param page the page number to retrieve
     * @return a Mono emitting a HousesApiResponse object representing the API response
     */
    public Mono<HousesApiResponse> getHouses(int page) {
        return WebClient.create(apiEndpoint) // Create a new WebClient instance targeting the API endpoint
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
                        .filter(this::shouldRetryOnError));
    }

    public Mono<String> downloadAndSavePhoto(HouseResponse house) {
        String photoUrl = house.getPhotoUrl();
        String id = house.getId();
        String address = house.getAddress();

        String extension = photoUtils.getExtensionFromUrl(photoUrl);

        String fileName = photoUtils.createFileName(id, address, extension);


        String filePath = photoUtils.createFilePath(photoFolder, fileName);

        photoUtils.createDirectoryIfNotExists(photoFolder);

        File file = new File(filePath);
        if (file.exists()) {
            log.info("Skipping download, file already exists: " + filePath);
            return Mono.just(fileName);
        }

        return downloadPhoto(photoUrl, file)
                .retryWhen(RetryBackoffSpec
                        .backoff(2, Duration.ofMillis(100))
                        .filter(this::shouldRetryOnError));
    }

    private Mono<String> downloadPhoto(String photoUrl, File file) {
        return WebClient.create()
                .get()
                .uri(photoUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .map(bytes -> {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        log.info("Saving the file: " + file.getPath());
                        fos.write(bytes);
                    } catch (IOException e) {
                        throw new RuntimeException("Error writing file: " + file.getPath(), e);
                    }
                    return file.getName();
                });
    }

    private boolean shouldRetryOnError(Throwable t) {
        return t instanceof WebClientResponseException.BadGateway ||
                t instanceof WebClientResponseException.GatewayTimeout ||
                t instanceof WebClientResponseException.ServiceUnavailable;
    }



}
