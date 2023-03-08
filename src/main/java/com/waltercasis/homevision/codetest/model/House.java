package com.waltercasis.homevision.codetest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class House {
    private String id;
    private String address;
    private String homeowner;
    private int price;
    @JsonProperty("photoURL")
    private String photoUrl;
}