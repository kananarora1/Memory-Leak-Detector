package com.example.devopsproj.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DereferenceRequest {

    @NotEmpty(message = "Object IDs list cannot be empty")
    private List<Long> objectIds;
}
