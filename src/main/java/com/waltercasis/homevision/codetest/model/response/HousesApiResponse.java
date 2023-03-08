package com.waltercasis.homevision.codetest.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HousesApiResponse {
    @JsonProperty("houses")
    private List<HouseResponse> houses;
    @JsonProperty("ok")
    private boolean ok;


}
