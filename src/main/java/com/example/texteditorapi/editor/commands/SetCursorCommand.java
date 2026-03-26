package com.example.texteditorapi.editor.commands;

import com.example.texteditorapi.editor.TextBuffer;

public final class SetCursorCommand implements Command {
    private final int pos;

    public SetCursorCommand(int pos) {
        this.pos = pos;
    }

    @Override
    public void apply(TextBuffer buffer) {
        buffer.setCursor(pos);
    }
}
