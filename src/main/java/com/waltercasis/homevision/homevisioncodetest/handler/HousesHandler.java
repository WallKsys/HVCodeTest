package com.waltercasis.homevision.homevisioncodetest.handler;

import com.waltercasis.homevision.homevisioncodetest.model.response.ApiResponse;
import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import com.waltercasis.homevision.homevisioncodetest.service.implementation.HouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/houses")
public class HousesHandler {


    private HouseService housesService;

    @Autowired
    public HousesHandler(HouseService housesService) {
        this.housesService = housesService;
    }

    /**
     * Retrieves a page of houses data and returns it as a reactive Mono.
     *
     * @param page the page number to retrieve
     * @return a Mono that emits a HousesApiResponse with the retrieved data
     */
    @GetMapping("/{page}")
    public Mono<HousesApiResponse> getHouses(@PathVariable("page") int page) {
        return housesService.getHouses(page) // Call the HousesService to retrieve a page of data
                .doOnError(throwable -> {
                    // If an error occurs, log the error message and throw a ResponseStatusException
                    log.error("Error getting houses: " + throwable.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting houses");
                });
    }


    /**
     * Downloads houses and photos for the specified page count and returns an ApiResponse.
     *
     * @param pageCount the number of pages to download
     * @return a Mono that emits an ApiResponse with the download status
     */
    @GetMapping("/download/{pageCount}")
    public Mono<ApiResponse> downloadHousesAndPhotos(@PathVariable("pageCount") int pageCount) {
        return housesService.getHousesAndPhotos(pageCount) // Call the HousesService to download houses and photos
                .flatMap(result -> Mono.just(new ApiResponse("success", "Houses and photos downloaded successfully", null)) // If successful, create a success ApiResponse
                        .map(apiResponse -> {
                            log.info("Houses and photos downloaded successfully");
                            return apiResponse;
                        })) // Log a success message and return the ApiResponse
                .onErrorResume(throwable -> {
                    // If an error occurs, log the error message and create an error ApiResponse with the error details
                    log.error("Error downloading houses and photos: " + throwable.getMessage());
                    return Mono.just(new ApiResponse("error", "Error downloading houses and photos",
                            Map.of(
                                    "timestamp", LocalDateTime.now(),
                                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                    "message", throwable.getMessage(),
                                    "path", "/download/" + pageCount
                            )));
                });
    }



}

