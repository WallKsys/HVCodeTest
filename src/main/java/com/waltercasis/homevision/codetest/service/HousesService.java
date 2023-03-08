package com.waltercasis.homevision.codetest.service;

import com.waltercasis.homevision.codetest.model.response.HouseResponse;
import com.waltercasis.homevision.codetest.model.response.HousesApiResponse;
import reactor.core.publisher.Mono;

public interface HousesService {

    Mono<HousesApiResponse> getHouses(int page);

    Mono<String> downloadAndSavePhoto(HouseResponse house);

    Mono<Void> getHousesAndPhotos(int pageCount);

}
