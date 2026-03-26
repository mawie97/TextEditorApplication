package com.example.texteditorapi.editor.api;

import com.example.texteditorapi.editor.commands.*;

public final class CommandFactory {
    private CommandFactory() {}

    public static Command from(CommandRequest r) {
        return switch (r.type()) {
            case INSERT -> new InsertCommand(requireText(r));

            case UNDO -> new UndoCommand();
            case REDO -> new RedoCommand();

            case MOVE_LEFT -> new MoveLeftCommand();
            case MOVE_RIGHT -> new MoveRightCommand();
            case MOVE_UP -> new MoveUpCommand();
            case MOVE_DOWN -> new MoveDownCommand();
            case MOVE_LINE_START -> new MoveToLineStartCommand();
            case MOVE_LINE_END -> new MoveToLineEndCommand();

            case DELETE_LEFT -> new DeleteLeftCommand();
            case DELETE_RIGHT -> new DeleteRightCommand();

            case SET_CURSOR -> new SetCursorCommand(requirePos(r));
            case SET_CURSOR_SELECTION -> new SetCursorSelectionCommand(requirePos(r));

            case MOVE_LEFT_SELECTION -> new MoveLeftSelectionCommand();
            case MOVE_RIGHT_SELECTION -> new MoveRightSelectionCommand();
            case MOVE_UP_SELECTION -> new MoveUpSelectionCommand();
            case MOVE_DOWN_SELECTION -> new MoveDownSelectionCommand();
            case MOVE_LINE_START_SELECTION -> new MoveToLineStartSelectionCommand();
            case MOVE_LINE_END_SELECTION -> new MoveToLineEndSelectionCommand();
        };
    }

    private static String requireText(CommandRequest r) {
        if (r.text() == null) {
            throw new IllegalArgumentException("text is required for INSERT");
        }
        return r.text();
    }

    private static int requirePos(CommandRequest r) {
        if (r.pos() == null) {
            throw new IllegalArgumentException("pos is required for this command");
        }
        return r.pos();
    }
}