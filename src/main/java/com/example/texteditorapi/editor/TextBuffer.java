package com.example.texteditorapi.editor;

import java.util.ArrayDeque;
import java.util.Deque;

public final class TextBuffer {

    public static final class Snapshot {
        public final String text;
        public final int cursor;
        public final int anchor;
        public final int preferredColumn;

        public Snapshot(String text, int cursor, int anchor, int preferredColumn) {
            if (text == null) throw new IllegalArgumentException("text cannot be null");
            this.text = text;
            this.cursor = cursor;
            this.anchor = anchor;
            this.preferredColumn = preferredColumn;
        }
    }

    public Snapshot snapshot() {
        return new Snapshot(
                text.toString(),
                cursor,
                anchor,
                preferredColumn
        );
    }

    public static TextBuffer fromSnapshot(Snapshot s) {
        if (s == null) throw new IllegalArgumentException("snapshot cannot be null");

        TextBuffer b = new TextBuffer(s.text);

        // Validate against the text length
        int len = b.text.length();
        if (s.cursor < 0 || s.cursor > len) throw new IllegalArgumentException("snapshot.cursor out of bounds");
        if (s.anchor < 0 || s.anchor > len) throw new IllegalArgumentException("snapshot.anchor out of bounds");
        if (s.preferredColumn < 0) throw new IllegalArgumentException("snapshot.preferredColumn cannot be negative");

        // Restore state (DO NOT call setCursor() because it clears selection)
        b.cursor = s.cursor;
        b.anchor = s.anchor;
        b.preferredColumn = s.preferredColumn;

        // Option A: history is empty on restore
        b.undo.clear();
        b.redo.clear();

        return b;
    }

    private enum EditType {
        INSERT,
        DELETE,
    }
    private static final class Edit {
        final EditType type;
        final int pos;
        final int cursorBefore;
        final int cursorAfter;
        final int anchorBefore;
        final int anchorAfter;
        final int colBefore;
        final int colAfter;
        final String deltaText;

        Edit(
                EditType type,
                int pos,
                int cursorBefore,
                int cursorAfter,
                int anchorBefore,
                int anchorAfter,
                int colBefore,
                int colAfter,
                String deltaText
        ) {
            if (type == null) throw new IllegalArgumentException("kind cannot be null");
            if (deltaText == null) throw new IllegalArgumentException("deltaText cannot be null");
            if (pos < 0) throw new IllegalArgumentException("pos cannot be negative");

            this.type = type;
            this.pos = pos;
            this.deltaText = deltaText;

            this.cursorBefore = cursorBefore;
            this.cursorAfter = cursorAfter;

            this.anchorBefore = anchorBefore;
            this.anchorAfter = anchorAfter;

            this.colBefore = colBefore;
            this.colAfter = colAfter;
        }
    }

    private final StringBuilder text;
    private int cursor;
    private int preferredColumn;
    private int anchor;

    private Deque<Edit> undo = new ArrayDeque<>();
    private Deque<Edit> redo = new ArrayDeque<>();

    public TextBuffer(){
        this("");
    }

    public TextBuffer(String initialText) {
        if (initialText == null) {
            throw new IllegalArgumentException("initialText cannot be null");
        }
        this.text = new StringBuilder(initialText);
        this.cursor = this.text.length();
        this.anchor = this.cursor;
        this.preferredColumn = getColumn();
    }

    public String getText() {
        return text.toString();
    }

    public int length(){
        return text.length();
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int pos) {
        setCursorCore(pos);
        clearSelection();
    }

    private void deleteSelection() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        text.delete(start, end);

        setCursorCore(start);
        clearSelection();
    }

    public void insert(String s) {
        if (s == null) {
            throw new IllegalArgumentException("s cannot be null");
        }

        if (s.isEmpty()) return;

        // If we insert new text on selected text, we must save the selected text in case of an undo
        if (hasSelection()) {

            EditType delete = EditType.DELETE;
            int cursorBeforeDeletion = cursor;
            int anchorBeforeDeletion = anchor;
            int colBeforeDeletion = preferredColumn;
            int posBeforeDeletion = getSelectionStart();
            String deletedText = getSelectedText();

            deleteSelection();

            int cursorAfterDeletion = cursor;
            int anchorAfterDeletion = anchor;
            int colAfterDeletion = preferredColumn;

            Edit editDeleted = new Edit(
                    delete,
                    posBeforeDeletion,
                    cursorBeforeDeletion,
                    cursorAfterDeletion,
                    anchorBeforeDeletion,
                    anchorAfterDeletion,
                    colBeforeDeletion,
                    colAfterDeletion,
                    deletedText
                    );

            undo.push(editDeleted);

        }

        EditType et = EditType.INSERT;
        int cursorBeforeInsertion = cursor;
        int anchorBeforeInsertion = anchor;
        int columnBeforeInsertion = preferredColumn;
        int posBeforeInsertion = cursor;

        text.insert(cursor, s);

        cursor += s.length();
        anchor = cursor;
        preferredColumn = getColumn();

        int cursorAfterInsertion = cursor;
        int anchorAfterInsertion = anchor;
        int columnAfterInsertion = preferredColumn;

        Edit editInserted = new Edit(
                et,
                posBeforeInsertion,
                cursorBeforeInsertion,
                cursorAfterInsertion,
                anchorBeforeInsertion,
                anchorAfterInsertion,
                columnBeforeInsertion,
                columnAfterInsertion,
                s
        );

        undo.push(editInserted);
        redo.clear();
    }

    public void moveLeft() {
        moveLeftCore();
        clearSelection();
    }

    public void moveRight() {
        moveRightCore();
        clearSelection();
    }

    public void moveUp() {
        moveUpCore();
        clearSelection();
    }

    public void moveDown() {
        moveDownCore();
        clearSelection();
    }

    public void moveToLineStart() {
        moveToLineStartCore();
        clearSelection();
    }

    public void moveToLineEnd() {
        moveToLineEndCore();
        clearSelection();
    }

    public void deleteLeft() {
        if (hasSelection()) {
            int pos = getSelectionStart();
            String deletedText = getSelectedText();

            int cursorBefore = cursor;
            int anchorBefore = anchor;
            int colBefore = preferredColumn;

            deleteSelection(); // should leave cursor at pos, clear selection, update preferredColumn

            int cursorAfter = cursor;
            int anchorAfter = anchor;
            int colAfter = preferredColumn;

            Edit e = new Edit(
                    EditType.DELETE,
                    pos,
                    cursorBefore,
                    cursorAfter,
                    anchorBefore,
                    anchorAfter,
                    colBefore,
                    colAfter,
                    deletedText
            );

            undo.push(e);
            redo.clear();
            return;
        }

        if (cursor == 0) {
            return;
        }

        int pos = cursor - 1;
        String deletedText = String.valueOf(text.charAt(pos));

        int cursorBefore = cursor;
        int anchorBefore = anchor;
        int colBefore = preferredColumn;

        text.deleteCharAt(pos);
        cursor--;
        anchor = cursor;
        preferredColumn = getColumn();

        int cursorAfter = cursor;
        int anchorAfter = anchor;
        int colAfter = preferredColumn;

        Edit e = new Edit(
                EditType.DELETE,
                pos,
                cursorBefore,
                cursorAfter,
                anchorBefore,
                anchorAfter,
                colBefore,
                colAfter,
                deletedText
        );

        undo.push(e);
        redo.clear();
    }

    public void deleteRight() {
        // Case A: selection exists -> delete selection (one DELETE edit)
        if (hasSelection()) {
            int pos = getSelectionStart();
            String deletedText = getSelectedText();

            int cursorBefore = cursor;
            int anchorBefore = anchor;
            int colBefore = preferredColumn;

            deleteSelection();

            int cursorAfter = cursor;
            int anchorAfter = anchor;
            int colAfter = preferredColumn;

            Edit e = new Edit(
                    EditType.DELETE,
                    pos,
                    cursorBefore,
                    cursorAfter,
                    anchorBefore,
                    anchorAfter,
                    colBefore,
                    colAfter,
                    deletedText
            );

            undo.push(e);
            redo.clear();
            return;
        }

        // Case C: no-op
        if (cursor >= length()) {
            return;
        }

        // Case B: delete one char at cursor (one DELETE edit)
        int pos = cursor;
        String deletedText = String.valueOf(text.charAt(pos));

        int cursorBefore = cursor;
        int anchorBefore = anchor;
        int colBefore = preferredColumn;

        text.deleteCharAt(pos);
        // cursor stays the same
        anchor = cursor;
        preferredColumn = getColumn();

        int cursorAfter = cursor;
        int anchorAfter = anchor;
        int colAfter = preferredColumn;

        Edit e = new Edit(
                EditType.DELETE,
                pos,
                cursorBefore,
                cursorAfter,
                anchorBefore,
                anchorAfter,
                colBefore,
                colAfter,
                deletedText
        );

        undo.push(e);
        redo.clear();
    }

    public int getLine() {
        int lines = 0;
        char newLine = '\n';

        for (int i = 0; i < cursor; i++) {
            char c = text.charAt(i);

            if (c == newLine) {
                lines++;
            }
        }

        return lines;
    }

    public int getColumn() {
        int column = 0;
        char c = '\n';

        for (int i = cursor-1; i >= 0; i--) {
            if (text.charAt(i) == c){
                break;
            }
            column++;
        }

        return column;
    }

    private int lineStart(int pos) {
        char c = '\n';
        int i = Math.min(pos, text.length());

        while (i > 0 && text.charAt(i - 1) != c) {
            i--;
        }

        return i;
    }

    private int lineEnd(int pos) {
        char c = '\n';
        int i = Math.min(pos, text.length());

        while (i < text.length() && text.charAt(i) != c) {
            i++;
        }

        return i;
    }

    public void startSelection() { anchor = cursor; }   // aka setAnchor
    public void clearSelection() { anchor = cursor; }

    public boolean hasSelection() { return anchor != cursor; }

    public int getSelectionStart() { return Math.min(anchor, cursor); }
    public int getSelectionEnd()   { return Math.max(anchor, cursor); }

    public String getSelectedText() {
        if (!hasSelection()) return "";
        return text.substring(getSelectionStart(), getSelectionEnd());
    }

    public void moveLeftSelection() {
        if (!hasSelection()) {
            startSelection();
        }
        moveLeftCore();
    }

    public void moveRightSelection() {
        if (!hasSelection()) {
            startSelection();
        }
        moveRightCore();
    }

    public void moveUpSelection() {
        if (!hasSelection()) {
            startSelection();
        }
        moveUpCore();
    }

    public void moveDownSelection() {
        if (!hasSelection()) {
            startSelection();
        }
        moveDownCore();
    }

    public void moveToLineStartSelection() {
        if (!hasSelection()) {
            startSelection();
        }
        moveToLineStartCore();
    }

    public void moveToLineEndSelection() {
        if (!hasSelection()) {
            startSelection();
        }
        moveToLineEndCore();
    }

    public void setCursorSelection(int pos) {
        if (!hasSelection()) {
            startSelection();
        }
        setCursorCore(pos);
    }

    private void moveLeftCore() {
        if (cursor <= 0) {
            return;
        }
        cursor--;
        preferredColumn = getColumn();
    }

    private void moveRightCore() {
        if (cursor >= length()) {
            return;
        }
        cursor++;
        preferredColumn = getColumn();
    }

    private void moveUpCore() {
        int curLineStart = lineStart(cursor);

        if (curLineStart == 0) {
            return;
        }

        int prevLineEndIndex = curLineStart - 1;
        int prevLineStart = lineStart(prevLineEndIndex);
        int prevLineLen = prevLineEndIndex - prevLineStart;

        int newCursor = prevLineStart + Math.min(this.preferredColumn, prevLineLen);
        setCursorVerticalCore(newCursor);

    }

    private void moveDownCore() {
        int curLineEnd = lineEnd(cursor);

        if (curLineEnd == text.length()) {
            return;
        }

        int nextLineStart = curLineEnd + 1;
        int nextLineEnd = lineEnd(nextLineStart);
        int nextLineLen = nextLineEnd - nextLineStart;

        int newCursor = nextLineStart + Math.min(this.preferredColumn, nextLineLen);
        setCursorVerticalCore(newCursor);

    }

    private void moveToLineStartCore() {
        setCursorCore(lineStart(cursor));
    }

    private void moveToLineEndCore() {
        setCursorCore(lineEnd(cursor));
    }

    private void setCursorCore(int pos) {
        if (pos < 0 || pos > text.length()) {
            throw new IllegalArgumentException("Cursor position cannot be less than 0 or longer than the full String length");
        }

        cursor = pos;
        preferredColumn = getColumn();
    }

    private void setCursorVerticalCore(int pos) {
        if (pos < 0 || pos > text.length()) {
            throw new IllegalArgumentException("Cursor out of bounds");
        }

        cursor = pos;
    }

    private void applyInsertAt(int pos, String s) {
        text.insert(pos, s);
    }

    private void applyDeleteRange(int start, int end) {
        text.delete(start, end);
    }

    private void restoreBefore(Edit e) {
        cursor = e.cursorBefore;
        anchor = e.anchorBefore;
        preferredColumn = e.colBefore;
    }

    private void restoreAfter(Edit e) {
        cursor = e.cursorAfter;
        anchor = e.anchorAfter;
        preferredColumn = e.colAfter;
    }

    public boolean undo() {
        if (undo.isEmpty()) {
            return false;
        }

        Edit e = undo.pop();

        // Reverse the text change
        if (e.type == EditType.INSERT) {
            // Undo insert = delete the inserted text
            applyDeleteRange(e.pos, e.pos + e.deltaText.length());
        } else { // DELETE
            // Undo delete = re-insert the deleted text
            applyInsertAt(e.pos, e.deltaText);
        }

        // Restore exact prior state
        restoreBefore(e);

        // Move edit to redo stack
        redo.push(e);
        return true;
    }

    public boolean redo() {
        if (redo.isEmpty()) {
            return false;
        }

        Edit e = redo.pop();

        // Re-apply the text change
        if (e.type == EditType.INSERT) {
            applyInsertAt(e.pos, e.deltaText);
        } else { // DELETE
            applyDeleteRange(e.pos, e.pos + e.deltaText.length());
        }

        // Restore exact post state
        restoreAfter(e);

        // Move edit back to undo stack
        undo.push(e);
        return true;
    }
}
