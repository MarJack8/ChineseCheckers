package test_game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import game.FieldColor;
import org.junit.jupiter.api.Test;

import game.Board;

public class TestBoard {

    @Test
    void equalsTest() {
        Board a = new Board(0);
        Board b = new Board(0);
        Board c = new Board(2);
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(!a.equals(c));
        b.changeFieldColor(b.getNode(5,5), FieldColor.PLAYER4);
        assertTrue(!a.equals(b));
        a.changeFieldColor(a.getNode(5,5), FieldColor.PLAYER4);
        assertTrue(a.equals(b));
    }

    @Test
    void testGetColor() {
        Board a = new Board(2);
        assertEquals(a.getNode(4, 6).getFieldColor().getColor(), "RED");
        assertEquals(a.getNode(14, 6).getFieldColor().getColor(), "DARKBLUE");
        assertEquals(a.getNode(6, 6).getFieldColor().getColor(), "WHITE");
    }

    @Test
    void testChangeFieldColor() {
        Board a = new Board(2);
        a.changeFieldColor(a.getNode(6,6), FieldColor.PLAYER4);
        assertEquals(a.getNode(6, 6).getFieldColor().getColor(), FieldColor.PLAYER4.getColor());
    }

    @Test
    void testhighlightLegalMoves() {
        Board a = new Board(2);
        assertTrue(a.getLegal((a.getNode(3,6))).contains(a.getNode(5,5)));
        assertTrue(a.getLegal((a.getNode(3,6))).contains(a.getNode(5,7)));
    }
}
