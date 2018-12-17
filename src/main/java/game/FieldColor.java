package game;

public enum FieldColor {
    NO_PLAYER("WHITE"),
    PLAYER1("RED"),
    PLAYER2("DARKBLUE"),
    PLAYER3("YELLOW"),
    PLAYER4("GREEN"),
    PLAYER5("GRAY"),
    PLAYER6("BLACK"),
    LEGAL("LIGHTBLUE");

    private final String color;

    FieldColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
