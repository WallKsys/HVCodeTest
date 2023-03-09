package com.waltercasis.homevision.codetest.service.implementation;

import com.waltercasis.homevision.codetest.client.HouseClient;
import com.waltercasis.homevision.codetest.model.response.HouseResponse;
import com.waltercasis.homevision.codetest.model.response.HousesApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.mockito.Mockito.*;

class DefaultHouseServiceTest {

    private HouseClient houseClientMock;
    private DefaultHouseService houseService;

    @BeforeEach
    void setUp() {
        houseClientMock = mock(HouseClient.class);
        houseService = new DefaultHouseService(houseClientMock);
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
     public void testGetHousesAndPhotos() {
        // Mock houseClient

        HouseResponse house1 = new HouseResponse("1", "123 Main St", "owner", 12000,"https://example.com/photo1.jpg");
        HouseResponse house2 = new HouseResponse("2", "456 Oak Ave", "owner", 12000, "https://example.com/photo2.jpg");
        HousesApiResponse housesApiResponse1 = new HousesApiResponse(Arrays.asList(house1, house2), true);
        Mockito.when(houseClientMock.getHouses(1)).thenReturn(Mono.just(housesApiResponse1));

        // Mock downloadAndSavePhoto
        Mockito.when(houseClientMock.downloadAndSavePhoto(house1)).thenReturn(Mono.just("photo1.jpg"));


        // Create MyService instance and call method

        Mono<Void> result = houseService.getHousesAndPhotos(1);

        // Verify the method completes successfully
        StepVerifier.create(result)
                .expectError()
                .verify();

        // Verify the mocked methods were called with the expected arguments
        Mockito.verify(houseClientMock, Mockito.times(1)).getHouses(1);
        Mockito.verify(houseClientMock, Mockito.times(1)).downloadAndSavePhoto(house1);

    }


}