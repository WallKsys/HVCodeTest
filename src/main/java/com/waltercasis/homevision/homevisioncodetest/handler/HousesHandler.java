package com.waltercasis.homevision.homevisioncodetest.handler;

import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import com.waltercasis.homevision.homevisioncodetest.service.implementation.HouseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/houses")
public class HousesHandler {


    private HouseService housesService;

    @Autowired
    public HousesHandler(HouseService housesService) {
        this.housesService = housesService;
    }

    @GetMapping("/{page}")
    public Mono<HousesApiResponse> getHouses(@PathVariable("page") int page) {
        return housesService.getHouses(page)
                .doOnError(throwable -> {
                    log.error("Error getting houses: " + throwable.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting houses");
                });
    }


    @GetMapping("/download/{pageCount}")
    public Mono<Void> downloadHousesAndPhotos(@PathVariable("pageCount") int pageCount) {
        return housesService.getHousesAndPhotos(pageCount, "/images")
                .doOnError(throwable -> {
                    log.error("Error downloading houses and photos: " + throwable.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error downloading houses and photos");
                });
    }
}

