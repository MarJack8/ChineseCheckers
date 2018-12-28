package client;

import communication.CCMessage;
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
    Board board;
    boolean gameOn = false;
    XConnection xcon;
    CCMessage msg;
    Listener lst;

    private void setBoard(int playersNum) {
        if (playersNum != 0) {
            gameOn = true;
        }
        this.board = new Board(playersNum);
    }

    private void waitForSignal() {
       new Thread( lst ).start();
    }

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

        setBoard(0);

        for (int y = 0; y < board.HEIGHT; ++y) {
            for (int x = 0; x < board.WIDTH; ++x) {
                board.getNode(y, x).setCenterY(DISPLAY_HEIGHT*(y)/(board.HEIGHT-1));
                board.getNode(y, x).setCenterX(DISPLAY_WIDTH*((x+1-0.5*(y%2))+2)/board.WIDTH);
                board.getNode(y, x).setRadius(RADIUS);
                board.getNode(y, x).setFill(Paint.valueOf(board.getNode(y, x).getColor()));
                root.getChildren().add(board.getNode(y, x));
            }
        }

        GridPane menu = new GridPane();
        menu.setMinWidth(140);
        menu.setMinHeight(DISPLAY_HEIGHT);
        menu.setStyle("-fx-border-color: black ; -fx-padding: 10 ;");
        menu.setVgap(10);
        root.getChildren().add(menu);

        Circle clientColor = new Circle(RADIUS/2);
        clientColor.setFill(Paint.valueOf("WHITE"));

        xcon = new XConnection();

        primaryStage.setOnCloseRequest(event -> {
            xcon.close();
        });

        Button joinGame = new Button("Dołącz do gry");

        Button pas = new Button("Pas");
        pas.setOnAction(event -> {
            try {
            	if( !lst.isItMyTurn() ) return;
                if ( xcon.xpass()) {
                    waitForSignal();
                }
            } catch (IOException exc) {}
        });

        Button startGame = new Button("Start");

        startGame.setOnAction(evt -> {
            try {
                setBoard(xcon.xstart());
                waitForSignal();
                for (int y = 0; y < board.HEIGHT; ++y) {
                    for (int x = 0; x < board.WIDTH; ++x) {
                        board.getNode(y, x).setCenterY(DISPLAY_HEIGHT*(y)/(board.HEIGHT-1));
                        board.getNode(y, x).setCenterX(DISPLAY_WIDTH*((x+1-0.5*(y%2))+2)/board.WIDTH);
                        board.getNode(y, x).setRadius(RADIUS);
                        board.getNode(y, x).setFill(Paint.valueOf(board.getNode(y, x).getColor()));
                        root.getChildren().add(board.getNode(y, x));
                    }
                }
            } catch (IOException exc) {}

        });

        Button addBot = new Button("Dodaj bota");

        addBot.setOnAction(evt -> {
            try {
                xcon.xaddBot();
            } catch (IOException exc) {}
        });

        menu.add(joinGame, 0, 1);
        menu.add(pas, 0, 2);
        menu.setAlignment(Pos.TOP_CENTER);

        Label yourColor = new Label("Twój kolor:");
        menu.add(yourColor, 0, 3);
        menu.add(clientColor, 1, 3);

        Button exit = new Button("Wyjdź");
        exit.setOnAction(evt -> {
            xcon.close();
            primaryStage.close();
        });

        menu.add(exit, 0, 4);

        joinGame.setOnAction(event -> {
            try {
                xcon.xconnect("127.0.0.1", 8060);
                lst = new Listener( board, xcon );

                clientColor.setFill(Paint.valueOf(FieldColor.values()[xcon.getId()].getColor()));

                if (xcon.getConnectionMessage().getSignal().equals("joined")) {
                    setBoard(xcon.xwaitForGameStart());
                    System.out.println( "###" );
                    waitForSignal();
                    for (int y = 0; y < board.HEIGHT; ++y) {
                        for (int x = 0; x < board.WIDTH; ++x) {
                            board.getNode(y, x).setCenterY(DISPLAY_HEIGHT*(y)/(board.HEIGHT-1));
                            board.getNode(y, x).setCenterX(DISPLAY_WIDTH*((x+1-0.5*(y%2))+2)/board.WIDTH);
                            board.getNode(y, x).setRadius(RADIUS);
                            board.getNode(y, x).setFill(Paint.valueOf(board.getNode(y, x).getColor()));
                            root.getChildren().add(board.getNode(y, x));
                        }

                    }
                }
                else if (xcon.getConnectionMessage().getSignal().equals("created")) {
                    menu.add(startGame, 0, 5);
                    menu.add(addBot, 0, 6);
                }
            } catch (UnknownHostException exc) {}
            catch (IOException exc) {}
        });

        s.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            try {
            	if( !lst.isItMyTurn() ) return;
                if (evt.getPickResult().getIntersectedNode() instanceof Field) {
                    if (board.isLegal((Field) evt.getPickResult().getIntersectedNode())) {
                       if (xcon.xmove(((Field) evt.getPickResult().getIntersectedNode()).getYCord(),((Field) evt.getPickResult().getIntersectedNode()).getXCord())) {
                           board.flushHighlighted();
                           waitForSignal();
                       }
                    } else {
                        board.flushHighlighted();
                        board.setHighlighted(xcon.xselect(((Field) evt.getPickResult().getIntersectedNode()).getYCord(), ((Field) evt.getPickResult().getIntersectedNode()).getXCord(), board));
                        board.highlight();
                    }
                }
            } catch (NullPointerException exc) {
                System.out.println("No Field clicked.");
            } catch (IOException exc) {}
        });

        primaryStage.setScene(s);
        primaryStage.show();
    }
}
