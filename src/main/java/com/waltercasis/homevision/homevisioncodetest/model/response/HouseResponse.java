package com.waltercasis.homevision.homevisioncodetest.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("address")
    private String address;
    @JsonProperty("homeowner")
    private String homeowner;
    @JsonProperty("price")
    private int price;
    @JsonProperty("photoURL")
    private String photoUrl;
}
