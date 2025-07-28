package com.ecommerce.backend.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Имя обязательно для заполнения")
    @Size(min = 1, max = 50, message = "Имя должно содержать от 1 до 50 символов")
    private String firstName;

    @NotBlank(message = "Фамилия обязательно для заполнения")
    @Size(min = 1, max = 50, message = "Фамилия должно содержать от 1 до 50 символов")
    private String lastName;
}
