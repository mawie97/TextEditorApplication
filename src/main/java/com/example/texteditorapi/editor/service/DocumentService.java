package com.example.texteditorapi.editor.service;

import com.example.texteditorapi.editor.TextBuffer;
import com.example.texteditorapi.editor.commands.Command;
import com.example.texteditorapi.editor.persistence.DocumentEntity;
import com.example.texteditorapi.editor.persistence.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repo;

    public DocumentService(DocumentRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public UUID create() {
        return create("Untitled document", "");
    }

    @Transactional
    public UUID create(String initialText) {
        return create("Untitled document", initialText);
    }

    /** Create a new document with initial text. */
    @Transactional
    public UUID create(String title, String initialText) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        String finalTitle = (title == null || title.isBlank()) ? "Untitled document" : title;
        String finalText = (initialText == null) ? "" : initialText;

        TextBuffer buffer = new TextBuffer(finalText);
        TextBuffer.Snapshot snap = buffer.snapshot();

        DocumentEntity entity = new DocumentEntity(
                id,
                finalTitle,
                snap.text,
                snap.cursor,
                snap.anchor,
                snap.preferredColumn,
                now,
                now
        );

        repo.save(entity);
        return id;
    }

    @Transactional(readOnly = true)
    public DocumentEntity get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No document with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<DocumentEntity> getAll() {
        return repo.findAll();
    }

    /** Apply one command to a document and return the updated snapshot. */
    @Transactional
    public TextBuffer.Snapshot apply(UUID id, Command cmd) {
        if (cmd == null) throw new IllegalArgumentException("cmd cannot be null");

        DocumentEntity entity = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No document with id: " + id));

        // Rebuild buffer from persisted snapshot (Option A: undo/redo not persisted)
        TextBuffer buffer = TextBuffer.fromSnapshot(new TextBuffer.Snapshot(
                entity.getText(),
                entity.getCursor(),
                entity.getAnchor(),
                entity.getPreferredColumn()
        ));

        // Apply command
        cmd.apply(buffer);

        // Persist updated snapshot
        TextBuffer.Snapshot updated = buffer.snapshot();
        entity.setText(updated.text);
        entity.setCursor(updated.cursor);
        entity.setAnchor(updated.anchor);
        entity.setPreferredColumn(updated.preferredColumn);
        entity.setUpdatedAt(Instant.now());

        repo.save(entity);

        return updated;
    }

    /** Optional: remove a document. */
    @Transactional
    public boolean delete(UUID id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
