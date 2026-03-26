package com.example.texteditorapi.editor.commands;

import com.example.texteditorapi.editor.TextBuffer;

public final class InsertCommand implements Command {
    private final String text;

    public InsertCommand(String text) {
        this.text = text;
    }

    @Override
    public void apply(TextBuffer buffer) {
        buffer.insert(text);
    }
}
