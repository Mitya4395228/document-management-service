package com.example.management.dto;

import jakarta.validation.constraints.NotEmpty;

public record DocumentCreateDTO(@NotEmpty String author, @NotEmpty String title) {

}
