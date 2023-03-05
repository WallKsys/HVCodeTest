package com.waltercasis.homevision.homevisioncodetest.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HousesApiResponse {
    @JsonProperty("houses")
    private List<HouseResponse> houses;
    @JsonProperty("ok")
    private boolean ok;
}
