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
        return houseClient.getHouses(page)
                .flatMap(housesApiResponse -> {
                    log.info("Houses: " + housesApiResponse.getHouses());
                    return Mono.just(housesApiResponse);
                })
                .doOnError(throwable -> log.error("Error getting houses: " + throwable.getMessage()));
    }

    @Override
    public Mono<String> downloadAndSavePhoto(HouseResponse house, String path) {
        return houseClient.downloadAndSavePhoto(house, path)
                .flatMap(s -> {
                    log.info("Photo saved: " + s);
                    return Mono.just(s);
                })
                .doOnError(throwable -> log.error("Error saving photo for house id" +house.getId() + ": " + throwable.getMessage()));
    }

    @Override
    public Mono<Void> getHousesAndPhotos(int pageCount, String downloadPath) {
        return Flux.range(1, pageCount)
                .flatMap(page -> houseClient.getHouses(page)
                        .flatMapMany(housesApiResponse -> Flux.fromIterable(housesApiResponse.getHouses()))
                        .flatMap(house -> houseClient.downloadAndSavePhoto(house, downloadPath))
                        .subscribeOn(Schedulers.parallel())
                )
                .then();
    }
}
