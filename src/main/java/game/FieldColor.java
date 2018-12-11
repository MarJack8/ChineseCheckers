package game;

public enum FieldColor {
    NO_PLAYER("WHITE"),
    PLAYER1("RED"),
    PLAYER2("BLUE"),
    PLAYER3("YELLOW"),
    PLAYER4("GREEN"),
    PLAYER5("GRAY"),
    PLAYER6("BLACK"),
    LEGAL("CHARTREUSE");

    private final String color;

    FieldColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
