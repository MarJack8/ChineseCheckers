package client;

import communication.CCMessage;
import game.Board;
import game.Field;
import game.FieldColor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;

public class Listener implements Runnable {
	
	XConnection xcon;
	Board board;
	boolean myturn;
	Client client;

	Listener( Client client ) {
		this.client = client;
		xcon = client.xcon;
		board = client.board;
		myturn = false;
	}
	
	public boolean isItMyTurn() {
		return myturn;
	}
	
	public void endTurn() {
		myturn = false;
		client.setTurnOff();
	}

	public void startTurn() {
		myturn = true;
		client.setTurnOn();
	}
	
	@Override
	public void run() {
		CCMessage msg;
		do {
			msg = xcon.recvSignal();
			if( msg.getSignal().equals("your_turn") ) {
				Platform.runLater(new Runnable() {
					@Override public void run() {
						startTurn();
					}
				});
				break;
			}
			else if( msg.getSignal().equals( "move" ) ) {
				Field[] fld = xcon.xgetMove(msg, board);
	            FieldColor clr = xcon.xgetColor(msg);
	            board.changeFieldColor(fld[1], clr);
	            board.changeFieldColor(fld[0], FieldColor.NO_PLAYER);
			}
			else if (msg.getSignal().equals("finished")) {
				break;
			}
		} while( true );
	}
	
}
