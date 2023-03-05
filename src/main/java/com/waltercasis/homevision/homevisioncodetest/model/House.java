package com.waltercasis.homevision.homevisioncodetest.model;

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
    private String photoUrl;
}