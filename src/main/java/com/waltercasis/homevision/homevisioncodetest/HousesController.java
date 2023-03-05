package com.waltercasis.homevision.homevisioncodetest;

import com.waltercasis.homevision.homevisioncodetest.client.HousesApiClient;
import com.waltercasis.homevision.homevisioncodetest.model.House;
import com.waltercasis.homevision.homevisioncodetest.model.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

@RestController
public class HousesController {
    private static final Logger log = LoggerFactory.getLogger(HousesController.class);
    private final HousesApiClient housesApiClient;

    @Autowired
    public HousesController(HousesApiClient housesApiClient) {
        this.housesApiClient = housesApiClient;
    }


        @GetMapping("/houses")
        public Mono<ResponseEntity<List<House>>> getHouses(@RequestParam(value = "page", defaultValue = "1") int page,
                                                           @RequestParam(value = "per_page", defaultValue = "10") int perPage) {
            return housesApiClient.getHouses(page, perPage)
                    .map(houses -> ResponseEntity.ok(houses))
                    .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Collections.singletonList(new House("-1", "Error desconocido al obtener las casas.", "", 0, "")))));
        }


}
