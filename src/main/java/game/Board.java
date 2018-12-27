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

    public Board(int playersNum) {
        int[][] pNums = {
                {0},
                {0},
                {1, 4},
                {1, 2, 6},
                {2, 3, 5, 6},
                {0},
                {1, 2, 3, 4, 5, 6}
        };

        board = new Field[HEIGHT][WIDTH];
        highlighted = new ArrayList<>();
        for (int y = 0; y < board.length; ++y) {
            for (int x = 0; x < board[0].length; ++x) {
                if (START_BOARD[y][x] == 0){
                    board[y][x] = new Field(0, y, x);
                }
                else if (START_BOARD[y][x] != -1) {
                    for (int i = 0; i < pNums[playersNum].length; ++i) {
                        if (START_BOARD[y][x] == pNums[playersNum][i]) {
                            board[y][x] = new Field(i+1, y, x);
                            break;
                        }
                        board[y][x] = new Field(0, y, x);
                    }
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

    public ArrayList<Field> getLegal(Field field) {
        this.flushHighlighted();
        this.highlightLegalMoves(field);

        return highlighted;
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
            this.getNode(field.getYCord(), field.getXCord()).setFill(Paint.valueOf(FieldColor.LEGAL.getColor()));
        }
    }

    public void setHighlighted(ArrayList<Field> hgh) {
        this.highlighted = hgh;
    }

    public void highlight() {
        for (Field f: highlighted) {
            highlightField(f);
        }
    }

    private void findAHop(Field field) {
        int y = field.getYCord(), x = field.getXCord();
        int sign;

        if (y % 2 == 1) {
            sign = -1;
        }
        else {
            sign = 1;
        }

        try {
            if (!this.getNode(y + 1*sign, x + 1*sign).getColor().equals("WHITE")) {
                if (this.getNode(y + 2*sign, x + 1*sign).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y + 2*sign, x + 1*sign))) {
                    highlightField(this.getNode(y + 2*sign, x + 1*sign));
                    findAHop(this.getNode(y + 2*sign, x + 1*sign));
                }
            }
        } catch (NullPointerException exc) {}

        try {
            if (!this.getNode(y + (-1*sign), x + 1*sign).getColor().equals("WHITE")) {
                if(this.getNode(y + (-2*sign), x + 1*sign).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y + (-2*sign), x + 1*sign))) {
                    highlightField(this.getNode(y + (-2*sign), x + 1*sign));
                    findAHop(this.getNode(y + (-2*sign), x + 1*sign));
                }
            }
        } catch (NullPointerException exc) {}

        try {
            if (!this.getNode(y - 1, x).getColor().equals("WHITE")) {
                if (this.getNode(y - 2, x + (-1*sign)).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y - 2, x + (-1*sign)))) {
                    highlightField(this.getNode(y - 2, x + (-1*sign)));
                    findAHop(this.getNode(y - 2, x + (-1*sign)));
                }
            }
        } catch (NullPointerException exc) {}

        try {
            if (!this.getNode(y + 1, x).getColor().equals("WHITE")) {
                if (this.getNode(y + 2, x + (-1*sign)).getColor().equals("WHITE") && !highlighted.contains(this.getNode(y + 2, x + (-1*sign)))) {
                    highlightField(this.getNode(y + 2, x + (-1*sign)));
                    findAHop(this.getNode(y + 2, x + (-1*sign)));
                }
            }
        } catch (NullPointerException exc) {}

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
