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


    @Value("${api.homevision.endpoint.url}")
    String apiEndpointUrl;

    @Value("${api.homevision.endpoint.path}")
    String apiEndpointPath;

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
        return WebClient.create(apiEndpointUrl) // Create a new WebClient instance targeting the API endpoint
                .get() // Issue a GET request
                .uri(uriBuilder -> uriBuilder // Build the request URI using a UriBuilder
                        .path(apiEndpointPath) // Set the path to "/houses"
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
        // Get the URL, ID, and address of the photo from the HouseResponse object
        String photoUrl = house.getPhotoUrl();
        String id = house.getId();
        String address = house.getAddress();

        // Get image file extension from URL
        String extension = photoUtils.getExtensionFromUrl(photoUrl);

        // Create a file name using the image ID, address, and extension
        String fileName = photoUtils.createFileName(id, address, extension);

        // Create the full path of the file to be saved
        String filePath = photoUtils.createFilePath(photoFolder, fileName);

        // Create the directory if it doesn't exist
        photoUtils.createDirectoryIfNotExists(photoFolder);

        // Check if the file already exists on the file system
        File file = new File(filePath);
        if (file.exists()) {
            log.info("Skipping download, file already exists: " + filePath);
            return Mono.just(fileName);
        }

        // Download the photo and save it to the specified file
        return downloadPhoto(photoUrl, file)
                .retryWhen(RetryBackoffSpec // Retry the request with exponential backoff if it fails with certain exceptions
                        .backoff(2, Duration.ofMillis(100))
                        .filter(this::shouldRetryOnError));
    }

    private Mono<String> downloadPhoto(String photoUrl, File file) {
        return WebClient.create() // create a new instance of WebClient
                .get() // create a GET request
                .uri(photoUrl) // set the URI to download from
                .retrieve() // perform the request and retrieve the response
                .bodyToMono(byte[].class) // convert the response to a Mono of byte array
                .map(bytes -> { // map the byte array to a file name
                    try (FileOutputStream fos = new FileOutputStream(file)) { // try to create a new FileOutputStream for the file
                        log.info("Saving the file: " + file.getPath());
                        fos.write(bytes); // write the byte array to the file
                    } catch (IOException e) { // catch any IOException that might occur
                        throw new RuntimeException("Error writing file: " + file.getPath(), e); // throw a RuntimeException if there's an error
                    }
                    return file.getName(); // return the name of the file
                });
    }


    /**
     Determines whether a download operation should be retried based on the type of exception that occurred.
     @param t the {@link Throwable} object representing the exception that occurred
     @return true if the download operation should be retried, false otherwise
     */
    private boolean shouldRetryOnError(Throwable t) {
        return t instanceof WebClientResponseException.BadGateway ||
                t instanceof WebClientResponseException.GatewayTimeout ||
                t instanceof WebClientResponseException.ServiceUnavailable;
    }



}
