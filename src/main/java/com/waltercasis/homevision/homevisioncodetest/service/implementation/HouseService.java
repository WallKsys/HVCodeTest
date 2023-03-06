package com.waltercasis.homevision.homevisioncodetest.service.implementation;

import com.waltercasis.homevision.homevisioncodetest.client.HouseClient;
import com.waltercasis.homevision.homevisioncodetest.model.response.HouseResponse;
import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import com.waltercasis.homevision.homevisioncodetest.service.IHousesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Slf4j
@Service
public class HouseService implements IHousesService {


    @Autowired
    private final HouseClient houseClient;

    public HouseService(HouseClient houseClient) {
        this.houseClient = houseClient;
    }

    @Override
    public Mono<HousesApiResponse> getHouses(int page) {
        // Call the getHouses() method of houseClient with the specified page number to retrieve a Mono of HousesApiResponse
        return houseClient.getHouses(page)
                // Log the houses retrieved in the response
                .flatMap(housesApiResponse -> {
                    log.info("Houses: " + housesApiResponse.getHouses());
                    // Return the original Mono of HousesApiResponse
                    return Mono.just(housesApiResponse);
                })
                // Log any error that occurs
                .doOnError(throwable -> log.error("Error getting houses: " + throwable.getMessage()));
    }

    @Override
    public Mono<String> downloadAndSavePhoto(HouseResponse house) {
        return houseClient.downloadAndSavePhoto(house) // calls the method to download and save a photo for the given house
                .flatMap(s -> { // if successful, logs a message and returns the file name of the saved photo
                    log.info("Photo saved: " + s);
                    return Mono.just(s);
                })
                .doOnError(throwable -> // if an error occurs, logs an error message
                        log.error("Error saving photo for house id" + house.getId() + ": " + throwable.getMessage())
                );
    }


    @Override
    public Mono<Void> getHousesAndPhotos(int pageCount) {
        return Flux.range(1, pageCount)  // Generate a stream of integers from 1 to pageCount
                .flatMap(page -> houseClient.getHouses(page)  // For each page, retrieve a list of houses from the API
                        .flatMapMany(housesApiResponse -> Flux.fromIterable(housesApiResponse.getHouses()))  // Transform the list of houses into a stream of individual houses
                        .flatMap(house -> houseClient.downloadAndSavePhoto(house))  // For each house, download and save the photo
                        .subscribeOn(Schedulers.parallel())  // Execute the operations on multiple threads
                )
                .then();  // Wait for all the operations to complete
    }

}
