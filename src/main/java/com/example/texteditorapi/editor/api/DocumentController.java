package com.example.texteditorapi.editor.api;

import com.example.texteditorapi.editor.persistence.DocumentEntity;
import com.example.texteditorapi.editor.DocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
public final class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    public DocumentStateResponse create(@Valid @RequestBody CreateDocumentRequest req) {
        UUID id = service.create(req.title(), req.text());
        DocumentEntity entity = service.get(id);
        return toResponse(entity);
    }

    @GetMapping("/{id}")
    public DocumentStateResponse get(@PathVariable UUID id) {
        DocumentEntity entity = service.get(id);
        return toResponse(entity);
    }

    @GetMapping
    public List<DocumentSummaryResponse> getAll() {
        List<DocumentSummaryResponse> summaries = new ArrayList<>();
        List<DocumentEntity> documents = service.getAll();

        for (DocumentEntity document : documents) {
            summaries.add(toSummaryResponse(document));
        }

        return summaries;
    }

    @PostMapping("/{id}/commands")
    public DocumentStateResponse apply(@PathVariable UUID id, @Valid @RequestBody CommandRequest req) {
        var cmd = CommandFactory.from(req);
        service.apply(id, cmd);
        DocumentEntity entity = service.get(id);
        return toResponse(entity);
    }

    private static DocumentStateResponse toResponse(DocumentEntity e) {
        return new DocumentStateResponse(
                e.getId(),
                e.getTitle(),
                e.getText(),
                e.getCursor(),
                e.getAnchor(),
                e.getPreferredColumn(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private static DocumentSummaryResponse toSummaryResponse(DocumentEntity e) {
        return new DocumentSummaryResponse(
                e.getId(),
                e.getTitle(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public record DocumentStateResponse(
            UUID id,
            String title,
            String text,
            int cursor,
            int anchor,
            int preferredColumn,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record DocumentSummaryResponse(
            UUID id,
            String title,
            Instant createdAt,
            Instant updatedAt
    ) {}

}