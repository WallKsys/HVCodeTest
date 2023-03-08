package com.waltercasis.homevision.codetest.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.waltercasis.homevision.codetest.model.response.ApiResponse;
import com.waltercasis.homevision.codetest.model.response.HousesApiResponse;
import com.waltercasis.homevision.codetest.service.implementation.DefaultHouseService;
import com.waltercasis.homevision.codetest.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class HousesControllerTest {

    @Mock
    private DefaultHouseService housesService;

    private String pathHouseApiResponse;

    private String pathApiResponse;

    private HousesController housesController;

    @BeforeEach
    void setup(){

        pathHouseApiResponse ="/housesResponse/housesResponseOk.json";
        pathApiResponse ="/housesResponse/apiResponseOk.json";
    }

    @Test
    void getHouses_shouldReturnApiResponseMonoWhenReceiveByPathAPageSuccessfully(){

        var houseApiResponse = TestUtils.getResourceAs(pathHouseApiResponse, new TypeReference<HousesApiResponse>() {});
        var apiResponse = new ApiResponse("success", "Houses retrieved successfully", houseApiResponse );

        Mockito.when(housesService.getHouses(1)).thenReturn(Mono.just(houseApiResponse));

        housesController = new HousesController(housesService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(housesController)
                .setControllerAdvice(new HouseControllerAdvice())
                .build();

        final Mono<ApiResponse> responseMono = housesController.getHouses(1);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.equals(apiResponse))
                .verifyComplete();

        Mockito.verify(housesService).getHouses(1);

    }
}
