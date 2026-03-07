package com.example.texteditorapi.editor.api;

public enum CommandType {
    INSERT,

    UNDO,
    REDO,

    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LINE_START,
    MOVE_LINE_END,

    DELETE_LEFT,
    DELETE_RIGHT,

    SET_CURSOR,
    SET_CURSOR_SELECTION,

    MOVE_LEFT_SELECTION,
    MOVE_RIGHT_SELECTION,
    MOVE_UP_SELECTION,
    MOVE_DOWN_SELECTION,
    MOVE_LINE_START_SELECTION,
    MOVE_LINE_END_SELECTION
}