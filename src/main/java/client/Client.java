package client;


import game.Board;
import game.Field;
import game.FieldColor;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Client extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final double DISPLAY_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight()*0.95, DISPLAY_WIDTH = DISPLAY_HEIGHT;
        final double RADIUS = 0.025*DISPLAY_HEIGHT;

        primaryStage.setTitle("Chinese Checkers");

        Group root = new Group();
        Scene s = new Scene(root, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        Board board = new Board();

        s.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            try {
                System.out.print(((Field) evt.getPickResult().getIntersectedNode()).getYCord() + " ");
                System.out.print(((Field) evt.getPickResult().getIntersectedNode()).getXCord() + " ");
                System.out.println(((Field) evt.getPickResult().getIntersectedNode()).getColor());

                if (board.isLegal((Field) evt.getPickResult().getIntersectedNode())) {
                    board.changeFieldColor((Field) evt.getPickResult().getIntersectedNode(), board.selected.getFieldColor());
                    board.changeFieldColor(board.selected, FieldColor.NO_PLAYER);
                    board.flushHighlighted();
                }
                else {
                    board.flushHighlighted();
                    board.selected = ((Field) evt.getPickResult().getIntersectedNode());
                    board.highlightLegalMoves(board.selected);
                }

            } catch (NullPointerException exc) {
                System.out.println("No Field clicked.");
            }
        });

        for (int y = 0; y < board.HEIGHT; ++y) {
            for (int x = 0; x < board.WIDTH; ++x) {
                board.getNode(y, x).setCenterY(DISPLAY_HEIGHT*(y)/(board.HEIGHT-1));
                board.getNode(y, x).setCenterX(DISPLAY_WIDTH*(x+1-0.5*(y%2))/board.WIDTH);
                board.getNode(y, x).setRadius(RADIUS);
                board.getNode(y, x).setFill(Paint.valueOf(board.getNode(y, x).getColor()));
                root.getChildren().add(board.getNode(y, x));
            }
        }

        primaryStage.setScene(s);
        primaryStage.show();
    }
}
