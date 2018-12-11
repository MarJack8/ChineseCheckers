package game;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class Field extends Circle {
    private FieldColor fieldColor;
    private int yCord, xCord;

    public Field(int i, int y, int x) {
        this.yCord = y;
        this.xCord = x;
        this.fieldColor = FieldColor.values()[i];
        this.setStroke(Paint.valueOf("BLACK"));
    }

    public Field(FieldColor color, int y, int x) {
        this.fieldColor = color;
        this.yCord = y;
        this.xCord = x;
        this.setStroke(Paint.valueOf("BLACK"));
    }

    static Field getNullField() {
        Field nullField = new Field(0, -1, -1);
        nullField.setStroke(Paint.valueOf("TRANSPARENT"));
        return nullField;
    }

    public void setColor(FieldColor color) {
        this.fieldColor = color;
    }

    public String getColor() throws NullPointerException {
        return this.fieldColor.getColor();
    }

    public FieldColor getFieldColor() {
        return this.fieldColor;
    }

    public int getYCord() {
        return yCord;
    }

    public int getXCord() {
        return xCord;
    }
}
