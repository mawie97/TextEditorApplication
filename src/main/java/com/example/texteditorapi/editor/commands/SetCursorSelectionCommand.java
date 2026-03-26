package com.example.texteditorapi.editor.commands;

import com.example.texteditorapi.editor.TextBuffer;

public final class SetCursorSelectionCommand implements Command {
    private final int pos;

    public SetCursorSelectionCommand(int pos) {
        this.pos = pos;
    }

    @Override
    public void apply(TextBuffer buffer) {
        buffer.setCursorSelection(pos);
    }
}