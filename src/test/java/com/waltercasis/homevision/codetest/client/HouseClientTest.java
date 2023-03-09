package com.waltercasis.homevision.codetest.client;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.waltercasis.homevision.codetest.model.response.HousesApiResponse;
import com.waltercasis.homevision.codetest.utils.PhotoUtils;
import com.waltercasis.homevision.codetest.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.waltercasis.homevision.codetest.utils.TestUtils.getResourceAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@TestPropertySource(locations = "classpath:application-test.properties")
class HouseClientTest {
    WireMockServer wireMockServer = new WireMockServer(8089);
    private  String PARAMETER_PATH="?page=1";

    @Mock
    private PhotoUtils photoUtils;

    @BeforeEach
    void setup(){
        if(!wireMockServer.isRunning()){
            wireMockServer.start();
        }
        photoUtils = mock(PhotoUtils.class);
    }

    @Test
    public void  getHouses_shouldResponseHousesApiResponseByPage(){
        ObjectMapper objectMapper = new ObjectMapper();
        wireMockServer.resetAll();
        HouseClient houseClient = new HouseClient(photoUtils);
        houseClient.apiEndpointPath="/houses";
        houseClient.photoFolder="images";
        houseClient.apiEndpointUrl="http://localhost:8089/";


        String path ="/housesResponse/housesResponseOk.json";

        String houseResponseString = getResourceAsString(path);

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/houses"+PARAMETER_PATH))

                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(houseResponseString)));

        var houseResponse = TestUtils.getResourceAs(path, new TypeReference<HousesApiResponse>() {});

        StepVerifier.create(houseClient.getHouses(1))
                .expectNextMatches(response -> {
                    assertEquals(houseResponse.getHouses(), response.getHouses());
                    if(houseResponse.equals(response)){
                        return true;
                    }else return false;
                }).expectComplete()
                .verify();
    }

    @AfterEach
    void finalTest(){
        wireMockServer.stop();
    }
}