package com.example.texteditorapi.editor.api;

import com.example.texteditorapi.editor.DocumentService;
import com.example.texteditorapi.editor.TextBuffer;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/documents")
public final class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    public DocumentStateResponse create() {
        UUID id = service.create("");
        return toResponse(id, service.get(id));
    }

    @GetMapping("/{id}")
    public DocumentStateResponse get(@PathVariable UUID id) {
        return toResponse(id, service.get(id));
    }

    @PostMapping("/{id}/commands")
    public DocumentStateResponse apply(@PathVariable UUID id, @Valid @RequestBody CommandRequest req) {
        var cmd = CommandFactory.from(req);
        TextBuffer.Snapshot s = service.apply(id, cmd);
        return toResponse(id, s);
    }

    private static DocumentStateResponse toResponse(UUID id, TextBuffer.Snapshot s) {
        return new DocumentStateResponse(id, s.text, s.cursor, s.anchor, s.preferredColumn);
    }

    public record DocumentStateResponse(
            UUID id,
            String text,
            int cursor,
            int anchor,
            int preferredColumn
    ) {}
}