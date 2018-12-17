package client;

import game.Board;
import game.Field;
import game.FieldColor;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.UnknownHostException;

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
        Scene s = new Scene(root, DISPLAY_WIDTH + 190, DISPLAY_HEIGHT);

        GridPane menu = new GridPane();
        menu.setMinWidth(140);
        menu.setMinHeight(DISPLAY_HEIGHT);
        menu.setStyle("-fx-border-color: black ; -fx-padding: 10 ;");
        menu.setVgap(10);
        root.getChildren().add(menu);

        Circle clientColor = new Circle(RADIUS/2);
        clientColor.setFill(Paint.valueOf("WHITE"));

        XConnection xcon = new XConnection();

        primaryStage.setOnCloseRequest(event -> {
            xcon.close();
        });

        Button joinGame = new Button("Dołącz do gry");
        joinGame.setOnAction(event -> {
            try {
                xcon.xconnect("127.0.0.1", 8060);

                if (xcon.getConnectionMessage().equals("joined")) {
                }
                else if (xcon.getConnectionMessage().equals("created")) {
                }

                clientColor.setFill(Paint.valueOf(FieldColor.values()[xcon.getId()].getColor()));
            } catch (UnknownHostException exc) {}
              catch (IOException exc) {}
        });

        Button pas = new Button("Pas");
        pas.setOnAction(event -> {
            try {
                xcon.xpass();
            } catch (IOException exc) {}
        });

        menu.add(joinGame, 0, 0);
        menu.add(pas, 0, 1);
        menu.setAlignment(Pos.TOP_CENTER);

        Label yourColor = new Label("Twój kolor:");
        menu.add(yourColor, 0, 2);

        menu.add(clientColor, 1, 2);

        Button exit = new Button("Wyjdź");
        exit.setOnAction(evt -> {
            xcon.close();
            primaryStage.close();
        });

        menu.add(exit, 0, 4);

        Board board = new Board(3);

        s.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            try {
                if (evt.getPickResult().getIntersectedNode() instanceof Field) {
                    if (board.isLegal((Field) evt.getPickResult().getIntersectedNode())) {
                        board.changeFieldColor((Field) evt.getPickResult().getIntersectedNode(), board.selected.getFieldColor());
                        board.changeFieldColor(board.selected, FieldColor.NO_PLAYER);
                        board.flushHighlighted();
                    } else {
                        board.flushHighlighted();
                        if (!((Field) evt.getPickResult().getIntersectedNode()).getColor().equals("WHITE")) {
                            board.selected = ((Field) evt.getPickResult().getIntersectedNode());
                            board.highlightLegalMoves(board.selected);
                        }
                    }
                }
            } catch (NullPointerException exc) {
                System.out.println("No Field clicked.");
            }
        });

        for (int y = 0; y < board.HEIGHT; ++y) {
            for (int x = 0; x < board.WIDTH; ++x) {
                board.getNode(y, x).setCenterY(DISPLAY_HEIGHT*(y)/(board.HEIGHT-1));
                board.getNode(y, x).setCenterX(DISPLAY_WIDTH*((x+1-0.5*(y%2))+2)/board.WIDTH);
                board.getNode(y, x).setRadius(RADIUS);
                board.getNode(y, x).setFill(Paint.valueOf(board.getNode(y, x).getColor()));
                root.getChildren().add(board.getNode(y, x));
            }
        }

        primaryStage.setScene(s);
        primaryStage.show();
    }
}
