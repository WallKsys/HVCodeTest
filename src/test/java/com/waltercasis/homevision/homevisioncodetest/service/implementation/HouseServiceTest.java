package com.waltercasis.homevision.homevisioncodetest.service.implementation;

import static org.mockito.Mockito.*;

import com.waltercasis.homevision.homevisioncodetest.client.HouseClient;
import com.waltercasis.homevision.homevisioncodetest.model.response.HouseResponse;
import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class HouseServiceTest {

    private HouseClient houseClientMock;
    private HouseService houseService;

    @BeforeEach
    void setUp() {
        houseClientMock = mock(HouseClient.class);
        houseService = new HouseService(houseClientMock);
    }

    @Test
    void getHouses_shouldReturnHousesApiResponse() {
        int page = 1;
        HousesApiResponse expectedResponse = new HousesApiResponse();
        when(houseClientMock.getHouses(page)).thenReturn(Mono.just(expectedResponse));

        Mono<HousesApiResponse> result = houseService.getHouses(page);

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
        verify(houseClientMock).getHouses(page);
    }

    @Test
    void downloadAndSavePhoto_shouldReturnFileName() {
        HouseResponse house = new HouseResponse();
        String expectedFileName = "photo.jpg";
        when(houseClientMock.downloadAndSavePhoto(house)).thenReturn(Mono.just(expectedFileName));

        Mono<String> result = houseService.downloadAndSavePhoto(house);

        StepVerifier.create(result)
                .expectNext(expectedFileName)
                .expectComplete()
                .verify();
        verify(houseClientMock).downloadAndSavePhoto(house);
    }

    @Test
    void getHousesAndPhotos_shouldComplete() {
        int pageCount = 2;
        when(houseClientMock.getHouses(anyInt())).thenReturn(Mono.just(new HousesApiResponse()));
        when(houseClientMock.downloadAndSavePhoto(any(HouseResponse.class))).thenReturn(Mono.empty());

        Mono<Void> result = houseService.getHousesAndPhotos(pageCount);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(houseClientMock, times(pageCount)).getHouses(anyInt());
        verify(houseClientMock, atLeastOnce()).downloadAndSavePhoto(any(HouseResponse.class));
    }

}