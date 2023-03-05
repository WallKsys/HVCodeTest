package com.waltercasis.homevision.homevisioncodetest.client;

import com.waltercasis.homevision.homevisioncodetest.model.House;
import com.waltercasis.homevision.homevisioncodetest.model.response.HouseResponse;
import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
@Component
public class HousesApiClient {

    private final WebClient webClient;

    public HousesApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://app-homevision-staging.herokuapp.com/api_project").build();
    }

    public Mono<List<House>> getHouses(int page, int perPage) {
        String housesUrl = buildHousesUrl(page, perPage);
        return fetchHousesFromServer(housesUrl)
                .doOnNext(houses -> houses.forEach(this::downloadHousePhoto))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error getting houses", e)))
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.boundedElastic());
    }

    private String buildHousesUrl(int page, int perPage) {
        return UriComponentsBuilder.fromPath("/houses")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .build()
                .toUriString();
    }

    private Mono<List<House>> fetchHousesFromServer(String housesUrl) {
        return webClient.get()
                .uri(housesUrl)
                .retrieve()
                .bodyToMono(HousesApiResponse.class)
                .flatMap(housesResponse -> Mono.just(processHousesResponse(housesResponse)));
    }

    private List<House> processHousesResponse(HousesApiResponse housesResponse) {
        List<House> houses = new ArrayList<>();
        if (housesResponse != null && housesResponse.isOk()) {
            for (HouseResponse houseResponse : housesResponse.getHouses()) {
                House house = new House(houseResponse.getId(),
                        houseResponse.getAddress(),
                        houseResponse.getHomeowner(),
                        houseResponse.getPrice(),
                        houseResponse.getPhotoUrl());
                houses.add(house);
            }
        }
        return houses;
    }

    public void downloadHousePhoto(House house) {
        String url = house.getPhotoUrl();
        String extension = FilenameUtils.getExtension(url);
        String filename = house.getId() + "-" + house.getAddress() + "." + extension;
        Mono<byte[]> result = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class);
        result.subscribe(bytes -> {
            try {
                FileUtils.writeByteArrayToFile(new File("photos/" + filename), bytes);
                log.info("Photo of house {} downloaded and saved successfully", house.getId());
            } catch (IOException e) {
                log.error("Error saving photo of house {}", house.getId(), e);
            }
        });
    }


}
