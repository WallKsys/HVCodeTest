package com.waltercasis.homevision.homevisioncodetest.client;

import com.waltercasis.homevision.homevisioncodetest.model.House;
import com.waltercasis.homevision.homevisioncodetest.model.response.HouseResponse;
import com.waltercasis.homevision.homevisioncodetest.model.response.HousesApiResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadGateway;
import org.springframework.web.reactive.function.client.WebClientResponseException.GatewayTimeout;
import org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetryBackoffSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class HouseClient {

    String API_ENDPOINT = "http://app-homevision-staging.herokuapp.com/api_project";

    public Mono<HousesApiResponse> getHouses(int page) {
        return WebClient.create(API_ENDPOINT)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/houses")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(HousesApiResponse.class)
                .retryWhen(RetryBackoffSpec
                        .backoff(2, Duration.ofMillis(100))
                        .filter(t -> t instanceof BadGateway ||
                                t instanceof GatewayTimeout ||
                                t instanceof ServiceUnavailable))
                .log();
    }

    public Mono<String> downloadAndSavePhoto(HouseResponse house, String path) {
        String photoUrl = house.getPhotoUrl();
        String id = house.getId();
        String address = house.getAddress();

        // Extraer la extensión del archivo de la URL de la foto
        String extension = photoUrl.substring(photoUrl.lastIndexOf(".") + 1);

        // Construir el nombre del archivo
        String fileName = id + "-" + address + "." + extension;
        String filePath = path + File.separator + fileName;

        return WebClient.create()
                .get()
                .uri(photoUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .map(bytes -> {
                    // Escribir los bytes de la foto en el archivo
                    File file = new File(filePath);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(bytes);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al escribir el archivo: " + filePath, e);
                    }
                    return fileName;
                })
                .retryWhen(RetryBackoffSpec
                        .backoff(2, Duration.ofMillis(100))
                        .filter(t -> t instanceof BadGateway ||
                                t instanceof GatewayTimeout ||
                                t instanceof ServiceUnavailable))
                .log();
    }






}
