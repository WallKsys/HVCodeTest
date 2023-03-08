package com.waltercasis.homevision.codetest.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class TestUtils {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    @SneakyThrows
    public static String getResourceAsString(String path){
        return Files.readString(Paths.get(Objects.requireNonNull(
                TestUtils.class.getResource(path)).toURI()), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static <T> T getResourceAs(String path, TypeReference<T> typeReference){
        return mapper.readValue(getResourceAsString(path), typeReference);
    }
}
