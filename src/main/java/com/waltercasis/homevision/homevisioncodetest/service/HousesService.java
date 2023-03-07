package com.waltercasis.homevision.homevisioncodetest.service;

import com.waltercasis.homevision.homevisioncodetest.model.response.HouseResponse;
import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import reactor.core.publisher.Mono;

public interface HousesService {

    Mono<HousesApiResponse> getHouses(int page);

    Mono<String> downloadAndSavePhoto(HouseResponse house);

    Mono<Void> getHousesAndPhotos(int pageCount);

}
