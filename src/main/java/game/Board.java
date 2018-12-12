package game;

import javafx.scene.paint.Paint;
import java.util.ArrayList;

public class Board {
    static private final int[][] START_BOARD = {
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},

            {-1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1},

            {-1, -1, -1, -1, -1, 1, 1, -1, -1, -1, -1, -1, -1},

            {-1, -1, -1, -1, -1, 1, 1, 1, -1, -1, -1, -1, -1},

            {-1, -1, -1, -1, 1, 1, 1, 1, -1, -1, -1, -1, -1},

            {6, 6, 6, 6,  0,  0,  0,  0,  0, 2, 2, 2, 2},

            {6, 6, 6,  0,  0,  0,  0,  0,  0, 2, 2, 2, -1},

            {-1, 6, 6,  0,  0,  0,  0,  0,  0,  0, 2, 2, -1},

            {-1, 6,  0,  0,  0,  0,  0,  0,  0,  0, 2, -1, -1},

            {-1, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -1},

            {-1, 5,  0,  0,  0,  0,  0,  0,  0,  0, 3, -1, -1},

            {-1, 5, 5,  0,  0,  0,  0,  0,  0,  0, 3, 3, -1},

            {5, 5, 5,  0,  0,  0,  0,  0,  0, 3, 3, 3, -1},

            {5, 5, 5, 5,  0,  0,  0,  0,  0, 3, 3, 3, 3},

            {-1, -1, -1, -1, 4, 4, 4, 4, -1, -1, -1, -1, -1},

            {-1, -1, -1, -1, -1, 4, 4, 4, -1, -1, -1, -1, -1},

            {-1, -1, -1, -1, -1, 4, 4, -1, -1, -1, -1, -1, -1},

            {-1, -1, -1, -1, -1, -1, 4, -1, -1, -1, -1, -1, -1},

            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
    };

    public final int HEIGHT = START_BOARD.length, WIDTH = START_BOARD[0].length;
    private Field board[][];
    public Field selected;
    private ArrayList<Field> highlighted;

    public Board() {
        board = new Field[HEIGHT][WIDTH];
        highlighted = new ArrayList<>();
        for (int y = 0; y < board.length; ++y) {
            for (int x = 0; x < board[0].length; ++x) {
                if (START_BOARD[y][x] != -1) {
                    board[y][x] = new Field(START_BOARD[y][x], y, x);
                } else {
                    board[y][x] = Field.getNullField();
                }
            }
        }
    }

    public Field getNode(int y, int x) {
        if (y >= HEIGHT || y < 0 || x >= WIDTH || x < 0)
            throw new NullPointerException();
        return board[y][x];
    }

    public boolean isLegal(Field field) {
        return highlighted.contains(field);
    }

    public void flushHighlighted() {
        for (Field field : highlighted) {
            field.setStroke(Paint.valueOf("BLACK"));
            field.setFill(Paint.valueOf(field.getColor()));
        }
        highlighted.clear();
    }

    public void changeFieldColor(Field field, FieldColor color) {
        if (field.getXCord() != -1) {
            this.getNode(field.getYCord(), field.getXCord()).setColor(color);
            this.getNode(field.getYCord(), field.getXCord()).setFill(Paint.valueOf(color.getColor()));
        }
    }

    private void highlightField(Field field) {
        if (field.getXCord() != -1 && field.getColor().equals("WHITE") && !(highlighted.contains(field))) {
            highlighted.add(field);
            this.getNode(field.getYCord(), field.getXCord()).setStroke(Paint.valueOf(FieldColor.LEGAL.getColor()));
            this.getNode(field.getYCord(), field.getXCord()).setFill(Paint.valueOf("MAGENTA"));
        }
    }

    private void findAHop(Field field) {
        int y = field.getYCord(), x = field.getXCord();

        if (y % 2 == 1) {

            try {
            if (!this.getNode(y - 1, x - 1).getColor().equals("WHITE")) {
               if(this.getNode(y - 2, x - 1).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y - 2, x - 1))) {
                   highlightField(this.getNode(y - 2, x - 1));
                       findAHop(this.getNode(y - 2, x - 1));
                   }
                }
            } catch (NullPointerException exc) {}

            try {
            if (!this.getNode(y + 1, x - 1).getColor().equals("WHITE")) {
                if(this.getNode(y + 2, x - 1).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y + 2, x - 1))) {
                    highlightField(this.getNode(y + 2, x - 1));
                        findAHop(this.getNode(y + 2, x - 1));
                    }
                }
            } catch (NullPointerException exc) {}

            try {
                if (!this.getNode(y - 1, x).getColor().equals("WHITE")) {
                if (this.getNode(y - 2, x + 1).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y - 2, x + 1))) {
                    highlightField(this.getNode(y - 2, x + 1));
                        findAHop(this.getNode(y - 2, x + 1));
                    }
                }
            } catch (NullPointerException exc) {}

            try {
            if (!this.getNode(y + 1, x).getColor().equals("WHITE")) {
                if (this.getNode(y + 2, x + 1).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y + 2, x + 1))) {
                    highlightField(this.getNode(y + 2, x + 1));
                        findAHop(this.getNode(y + 2, x + 1));
                    }
                }
            } catch (NullPointerException exc) {}

        } else {

            try {
            if (!this.getNode(y + 1, x + 1).getColor().equals("WHITE")) {
                if (this.getNode(y + 2, x + 1).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y + 2, x + 1))) {
                    highlightField(this.getNode(y + 2, x + 1));
                        findAHop(this.getNode(y + 2, x + 1));
                    }
                }
            } catch (NullPointerException exc) {}

            try {
            if (!this.getNode(y - 1, x + 1).getColor().equals("WHITE")) {
                if (this.getNode(y - 2, x + 1).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y - 2, x + 1))) {
                    highlightField(this.getNode(y - 2, x + 1));
                        findAHop(this.getNode(y - 2, x + 1));
                    }
                }
            } catch (NullPointerException exc) {}

            try {
            if (!this.getNode(y - 1, x).getColor().equals("WHITE")) {
                if (this.getNode(y - 2, x - 1).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y - 2, x - 1))) {
                    highlightField(this.getNode(y - 2, x - 1));
                        findAHop(this.getNode(y - 2, x - 1));
                    }
                }
            } catch (NullPointerException exc) {}

            try {
            if (!this.getNode(y + 1, x).getColor().equals("WHITE")) {
                if(this.getNode(y + 2, x - 1).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y + 2, x - 1))) {
                    highlightField(this.getNode(y + 2, x - 1));
                        findAHop(this.getNode(y + 2, x - 1));
                    }
                }
            } catch (NullPointerException exc) {}

        }

        try {
        if (!this.getNode(y, x - 1).getColor().equals("WHITE")) {
            if(this.getNode(y, x - 2).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y, x - 2))) {
                highlightField(this.getNode(y, x - 2));
                findAHop(this.getNode(y, x - 2));
            }
        }
        } catch (NullPointerException exc) {}

        try {
        if (!this.getNode(y, x + 1).getColor().equals("WHITE")) {
            if(this.getNode(y, x + 2).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y, x + 2))) {
                    highlightField(this.getNode(y, x + 2));
                    findAHop(this.getNode(y, x + 2));
                }
            }
        }catch (NullPointerException exc) {}
    }


    public void highlightLegalMoves(Field selected) {
        if (!selected.getColor().equals("WHITE")) {
            int ySelected = selected.getYCord();
            int xSelected = selected.getXCord();

            if (ySelected % 2 == 1) {
                highlightField(this.getNode(ySelected - 1, xSelected - 1));
                highlightField(this.getNode(ySelected + 1, xSelected - 1));
            } else {
                highlightField(this.getNode(ySelected - 1, xSelected + 1));
                highlightField(this.getNode(ySelected + 1, xSelected + 1));
            }
            highlightField(this.getNode(ySelected - 1, xSelected));
            highlightField(this.getNode(ySelected + 1, xSelected));
            highlightField(this.getNode(ySelected, xSelected - 1));
            highlightField(this.getNode(ySelected, xSelected + 1));
        }

        findAHop(selected);
    }
}
