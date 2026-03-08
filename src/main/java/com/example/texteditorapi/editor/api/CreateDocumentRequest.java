package com.example.texteditorapi.editor.api;

import jakarta.validation.constraints.Size;

public record CreateDocumentRequest(
        @Size(max = 255, message = "title must be at most 255 characters")
        String title,

        String text
) {}