package com.waltercasis.homevision.homevisioncodetest.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    String status;
    String message;
    Object data;

}
