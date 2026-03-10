package com.example.texteditorapi.editor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextBufferTest {

    @Test
    void getLine_singleLineIsZero() {
        TextBuffer tb = new TextBuffer("123");
        assertEquals(0, tb.getLine());
        tb.setCursor(0);
        assertEquals(0, tb.getLine());
    }

    @Test
    void getLine_emptyLineIsZero() {
        TextBuffer tb = new TextBuffer("");
        assertEquals(0, tb.getLine());
    }

    @Test
    void getLine_countsNewLinesBeforeCursor() {
        TextBuffer tb = new TextBuffer("1\n2\n3");
        assertEquals(2, tb.getLine());

        tb.setCursor(0);
        assertEquals(0, tb.getLine());

        tb.setCursor(2);
        assertEquals(1, tb.getLine());

        tb.setCursor(4);
        assertEquals(2, tb.getLine());
    }

    @Test
    void getColumn_columnsBeforeCursorSingleLine() {
        TextBuffer tb = new TextBuffer("123");

        tb.setCursor(0);
        assertEquals(0, tb.getColumn());

        tb.moveRight();
        assertEquals(1, tb.getColumn());

        tb.moveRight();
        assertEquals(2, tb.getColumn());

        tb.moveRight();
        assertEquals(3, tb.getColumn());
    }

    @Test
    void getColumn_columnsBeforeCursorMultipleLines() {
        TextBuffer tb = new TextBuffer("12\n34");

        tb.setCursor(2);
        assertEquals(2, tb.getColumn());

        tb.moveRight();
        assertEquals(0, tb.getColumn());

        tb.moveRight();
        assertEquals(1, tb.getColumn());

        tb.moveRight();
        assertEquals(2, tb.getColumn());
    }

    @Test
    void moveToLineEnd() {
        TextBuffer tb = new TextBuffer("12\n34");

        tb.setCursor(0);
        tb.moveToLineEnd();
        assertEquals(2, tb.getColumn());

        tb.setCursor(1);
        tb.moveToLineEnd();
        assertEquals(2, tb.getColumn());

        tb.setCursor(2);
        tb.moveToLineEnd();
        assertEquals(2, tb.getColumn());

        tb.setCursor(3);
        tb.moveToLineEnd();
        assertEquals(2, tb.getColumn());

        tb.setCursor(4);
        tb.moveToLineEnd();
        assertEquals(2, tb.getColumn());

        tb.setCursor(5);
        tb.moveToLineEnd();
        assertEquals(2, tb.getColumn());

    }

    @Test
    void moveToLineStart() {
        TextBuffer tb = new TextBuffer("12\n34");

        tb.setCursor(0);
        tb.moveToLineStart();
        assertEquals(0, tb.getColumn());

        tb.setCursor(1);
        tb.moveToLineStart();
        assertEquals(0, tb.getColumn());

        tb.setCursor(2);
        tb.moveToLineStart();
        assertEquals(0, tb.getColumn());

        tb.setCursor(3);
        tb.moveToLineStart();
        assertEquals(0, tb.getColumn());

        tb.setCursor(4);
        tb.moveToLineStart();
        assertEquals(0, tb.getColumn());

        tb.setCursor(5);
        tb.moveToLineStart();
        assertEquals(0, tb.getColumn());

    }

    @Test
    void moveUp() {
        TextBuffer tb = new TextBuffer("12\n456");

        tb.setCursor(0);
        tb.moveUp();
        assertEquals(0, tb.getLine());
        assertEquals(0, tb.getColumn());
        assertEquals(0, tb.getCursor());

        tb.setCursor(1);
        tb.moveUp();
        assertEquals(0, tb.getLine());
        assertEquals(1, tb.getColumn());
        assertEquals(1, tb.getCursor());

        tb.setCursor(2);
        tb.moveUp();
        assertEquals(2, tb.getCursor());

        tb.setCursor(3);
        tb.moveUp();
        assertEquals(0, tb.getCursor());

        tb.setCursor(4);
        tb.moveUp();
        assertEquals(1, tb.getCursor());

        tb.setCursor(5);
        tb.moveUp();
        assertEquals(2, tb.getCursor());

        tb.setCursor(6);
        tb.moveUp();
        assertEquals(2, tb.getCursor());
    }

    @Test
    void moveDown() {
        TextBuffer tb = new TextBuffer("123\n45");

        tb.setCursor(0);
        tb.moveDown();
        assertEquals(4, tb.getCursor());

        tb.setCursor(1);
        tb.moveDown();
        assertEquals(5, tb.getCursor());

        tb.setCursor(2);
        tb.moveDown();
        assertEquals(6, tb.getCursor());

        tb.setCursor(3);
        tb.moveDown();
        assertEquals(6, tb.getCursor());

        tb.setCursor(4);
        tb.moveDown();
        assertEquals(4, tb.getCursor());

        tb.setCursor(5);
        tb.moveDown();
        assertEquals(5, tb.getCursor());

        tb.setCursor(6);
        tb.moveDown();
        assertEquals(6, tb.getCursor());
    }

    @Test
    void preferredColumn_preservedAcrossDownDown() {
        TextBuffer tb = new TextBuffer("012\nx\n345");

        tb.setCursor(3);
        assertEquals(0, tb.getLine());
        assertEquals(3, tb.getColumn());

        tb.moveDown();
        assertEquals(1, tb.getLine());
        assertEquals(1, tb.getColumn());

        tb.moveDown();
        assertEquals(2, tb.getLine());
        assertEquals(3, tb.getColumn());
    }

    @Test
    void preferredColumn_resetsAfterHorizontalMove() {
        TextBuffer tb = new TextBuffer("012\nxy\n345");

        tb.setCursor(3);
        tb.moveDown();
        assertEquals(1, tb.getLine());
        assertEquals(2, tb.getColumn());

        tb.moveLeft();
        assertEquals(1, tb.getLine());
        assertEquals(1, tb.getColumn());

        tb.moveDown();
        assertEquals(2, tb.getLine());
        assertEquals(1, tb.getColumn());
    }

    @Test
    void selection_basics_and_getSelectedText() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(2);
        tb.moveRightSelection();
        assertTrue(tb.hasSelection());
        assertEquals("c", tb.getSelectedText());

        tb.moveLeftSelection();
        assertFalse(tb.hasSelection());
        assertEquals("", tb.getSelectedText());

    }

    @Test
    void selection_growsLeft_selectsCharToLeft() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(2); // between b|c
        tb.moveLeftSelection(); // selects 'b'
        assertTrue(tb.hasSelection());
        assertEquals("b", tb.getSelectedText());

        tb.moveLeftSelection();
        assertEquals("ab", tb.getSelectedText());
    }

    @Test
    void selection_moveLeft_noCharOnLeftSide() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(0); // between b|c
        tb.moveLeftSelection(); // selects 'b'
        assertFalse(tb.hasSelection());
        assertEquals("", tb.getSelectedText());
    }

    @Test
    void selection_growsRight_selectsCharToRight() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(3); // between b|c
        tb.moveRightSelection(); // selects 'b'
        assertTrue(tb.hasSelection());
        assertEquals("d", tb.getSelectedText());

        tb.moveRightSelection();
        assertEquals("de", tb.getSelectedText());
    }

    @Test
    void selection_moveRight_noCharOnRight() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(5); // between b|c
        tb.moveRightSelection(); // selects 'b'
        assertFalse(tb.hasSelection());
        assertEquals("", tb.getSelectedText());
    }

    @Test
    void selection_reverseDirection_shrinksThenGrowsOtherSide() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(2);            // between b|c
        tb.moveRightSelection();    // select 'c' => [2,3)
        tb.moveRightSelection();    // select 'cd' => [2,4)
        assertEquals("cd", tb.getSelectedText());

        tb.moveLeftSelection();     // back to [2,3)
        assertEquals("c", tb.getSelectedText());

        tb.moveLeftSelection();     // collapses to empty at cursor=2
        assertFalse(tb.hasSelection());
        assertEquals("", tb.getSelectedText());

        tb.moveLeftSelection();     // now starts new selection left => selects 'b'
        assertTrue(tb.hasSelection());
        assertEquals("b", tb.getSelectedText());
    }

    @Test
    void insert_replacesSelection_andClearsSelection() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(1);           // a|bcde
        tb.moveRightSelection();   // select 'b'
        tb.moveRightSelection();   // select 'bc'
        assertEquals("bc", tb.getSelectedText());

        tb.insert("X");
        assertEquals("aXde", tb.getText());
        assertFalse(tb.hasSelection());
        assertEquals("", tb.getSelectedText());
        assertEquals(2, tb.getCursor()); // after inserted "X"
    }

    @Test
    void deleteLeft_deletesSelection() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(1);
        tb.moveRightSelection();
        tb.moveRightSelection(); // select "bc"
        assertEquals("bc", tb.getSelectedText());

        tb.deleteLeft();
        assertEquals("ade", tb.getText());
        assertFalse(tb.hasSelection());
        assertEquals(1, tb.getCursor()); // cursor goes to start of deleted range
    }

    @Test
    void deleteRight_deletesSelection() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(3);
        tb.moveLeftSelection();
        tb.moveLeftSelection(); // select "bc" (anchor 3, cursor 1)
        assertEquals("bc", tb.getSelectedText());

        tb.deleteRight();
        assertEquals("ade", tb.getText());
        assertFalse(tb.hasSelection());
        assertEquals(1, tb.getCursor());
    }

    @Test
    void selection_acrossLines_includesNewlineWhenCovered() {
        TextBuffer tb = new TextBuffer("ab\ncd");

        tb.setCursor(1);            // a|b\ncd
        tb.moveRightSelection();    // selects "b"
        tb.moveRightSelection();    // selects "b\n" (newline is a char)
        tb.moveRightSelection();    // selects "b\nc"
        assertEquals("b\nc", tb.getSelectedText());
    }

    @Test
    void selection_moveToLineEndSelection_selectsToEndOfLine() {
        TextBuffer tb = new TextBuffer("abcdef");

        tb.setCursor(2); // on second line: after '\n', cursor at 'c' => col 0
        tb.moveToLineEndSelection(); // select "cdef"
        assertEquals("cdef", tb.getSelectedText());
    }

    @Test
    void selection_moveToLineStartSelection_selectsToStartOfLine() {
        TextBuffer tb = new TextBuffer("abcdef");

        tb.setCursor(3); // on second line: after '\n', cursor at 'c' => col 0
        tb.moveToLineStartSelection(); // select "cdef"
        assertEquals("abc", tb.getSelectedText());
    }

    @Test
    void selection_setSelection() {
        TextBuffer tb = new TextBuffer("abcdef");

        tb.setCursorSelection(3);
        assertEquals("def", tb.getSelectedText());
    }

    @Test
    void selection_moveUpSelection() {
        TextBuffer tb = new TextBuffer("abc\nde");

        tb.setCursor(4);
        tb.moveUpSelection();
        assertEquals("abc\n", tb.getSelectedText());

        tb.setCursor(3);
        tb.moveUpSelection();
        assertEquals("", tb.getSelectedText());
    }

    @Test
    void selection_moveDownSelection() {
        TextBuffer tb = new TextBuffer("abc\nde");

        tb.setCursor(3);
        tb.moveDownSelection();

        assertEquals("\nde", tb.getSelectedText());
    }

    @Test
    void undoRedo_insert_singleChar() {
        TextBuffer tb = new TextBuffer("abc");
        tb.setCursor(1);              // a|bc
        tb.insert("X");               // aX|bc

        assertEquals("aXbc", tb.getText());
        assertEquals(2, tb.getCursor());

        assertTrue(tb.undo());
        assertEquals("abc", tb.getText());
        assertEquals(1, tb.getCursor());

        assertTrue(tb.redo());
        assertEquals("aXbc", tb.getText());
        assertEquals(2, tb.getCursor());
    }

    @Test
    void undoRedo_deleteLeft_singleChar() {
        TextBuffer tb = new TextBuffer("abc");
        tb.setCursor(2);              // ab|c
        tb.deleteLeft();              // a|c

        assertEquals("ac", tb.getText());
        assertEquals(1, tb.getCursor());

        assertTrue(tb.undo());
        assertEquals("abc", tb.getText());
        assertEquals(2, tb.getCursor());

        assertTrue(tb.redo());
        assertEquals("ac", tb.getText());
        assertEquals(1, tb.getCursor());
    }

    @Test
    void undoRedo_deleteRight_singleChar() {
        TextBuffer tb = new TextBuffer("abc");
        tb.setCursor(1);              // a|bc
        tb.deleteRight();             // a|c

        assertEquals("ac", tb.getText());
        assertEquals(1, tb.getCursor());

        assertTrue(tb.undo());
        assertEquals("abc", tb.getText());
        assertEquals(1, tb.getCursor());

        assertTrue(tb.redo());
        assertEquals("ac", tb.getText());
        assertEquals(1, tb.getCursor());
    }

    @Test
    void undoRedo_replaceSelection_isTwoUndos() {
        TextBuffer tb = new TextBuffer("abcde");

        tb.setCursor(1);              // a|bcde
        tb.moveRightSelection();
        tb.moveRightSelection();      // selects "bc"
        assertEquals("bc", tb.getSelectedText());

        tb.insert("X");               // delete "bc" then insert "X"
        assertEquals("aXde", tb.getText());

        // Undo #1: undo the INSERT "X"
        assertTrue(tb.undo());
        assertEquals("ade", tb.getText());

        // Undo #2: undo the DELETE "bc"
        assertTrue(tb.undo());
        assertEquals("abcde", tb.getText());
    }

    @Test
    void redo_isClearedAfterNewEdit() {
        TextBuffer tb = new TextBuffer("abc");
        tb.setCursor(3);
        tb.insert("X");               // abcX
        assertEquals("abcX", tb.getText());

        assertTrue(tb.undo());
        assertEquals("abc", tb.getText());

        // New edit after undo clears redo history
        tb.insert("Y");               // abcY
        assertEquals("abcY", tb.getText());

        assertFalse(tb.redo());
        assertEquals("abcY", tb.getText());
    }

    @Test
    void undoRedo_restoresSelectionState_forSelectionDelete() {
        TextBuffer tb = new TextBuffer("abcdef");

        tb.setCursor(2);
        tb.moveRightSelection();
        tb.moveRightSelection();      // selects "cd"
        assertEquals("cd", tb.getSelectedText());

        tb.deleteLeft();              // deletes selection
        assertEquals("abef", tb.getText());
        assertFalse(tb.hasSelection());

        assertTrue(tb.undo());
        assertEquals("abcdef", tb.getText());
        // selection restored (since you restore anchorBefore/cursorBefore)
        assertTrue(tb.hasSelection());
        assertEquals("cd", tb.getSelectedText());
    }

    @Test
    void snapshotRoundTripRestoresExactState() {
        TextBuffer b = new TextBuffer("hello\nworld");
        b.moveLeft();
        b.moveLeft();
        b.startSelection();
        b.moveLeft();
        b.insert("X");

        TextBuffer.Snapshot s = b.snapshot();
        TextBuffer restored = TextBuffer.fromSnapshot(s);

        assertEquals(b.getText(), restored.getText());
        assertEquals(b.getCursor(), restored.getCursor());
        assertEquals(b.getSelectionStart(), restored.getSelectionStart());
        assertEquals(b.getSelectionEnd(), restored.getSelectionEnd());
    }

    @Test
    void undoHistoryIsNotRestored() {
        TextBuffer b = new TextBuffer("abc");
        b.insert("x");

        TextBuffer restored = TextBuffer.fromSnapshot(b.snapshot());

        assertFalse(restored.undo());
    }

    @Test
    void invalidSnapshotThrows() {
        TextBuffer.Snapshot s =
                new TextBuffer.Snapshot("abc", 99, 0, 0);

        assertThrows(IllegalArgumentException.class,
                () -> TextBuffer.fromSnapshot(s));
    }

    


}


