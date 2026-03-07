package com.example.texteditorapi.editor.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CommandRequest(
        @NotNull(message = "type is required")
        CommandType type,

        String text,

        @PositiveOrZero(message = "pos must be zero or greater")
        Integer pos
) {}