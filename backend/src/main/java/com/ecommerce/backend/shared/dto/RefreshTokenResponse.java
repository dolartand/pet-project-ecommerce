package com.ecommerce.backend.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private String accessToken;
    private String refreshToken;
}