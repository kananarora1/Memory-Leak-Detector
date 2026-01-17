package com.example.devopsproj.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateRequest {

    @NotNull(message = "Count is required")
    @Min(value = 1, message = "Count must be at least 1")
    private Integer count;

    @NotNull(message = "Size is required")
    @Min(value = 1, message = "Size must be at least 1 KB")
    private Integer sizeInKB;

    @NotBlank(message = "Object type is required")
    private String objectType;

    @Builder.Default
    private Boolean createReferences = true;
}
