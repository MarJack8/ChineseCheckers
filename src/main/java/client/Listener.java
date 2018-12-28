package client;

import communication.CCMessage;
import game.Board;
import game.Field;
import game.FieldColor;
import javafx.application.Application;
import javafx.scene.control.Label;

public class Listener implements Runnable {
	
	XConnection xcon;
	Board board;
	boolean myturn;
	Label showTurn;
	Client client;

	Listener( Client client ) {
		this.client = client;
		xcon = client.xcon;
		board = client.board;
		showTurn = client.showTurn;
		myturn = false;
	}
	
	public boolean isItMyTurn() {
		return myturn;
	}
	
	public void endTurn() {
		myturn = false;
		showTurn.setText("");
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
				startTurn();
				break;
			}
			else if( msg.getSignal().equals( "move" ) ) {
				Field[] fld = xcon.xgetMove(msg, board);
	            FieldColor clr = xcon.xgetColor(msg);
	            board.changeFieldColor(fld[1], clr);
	            board.changeFieldColor(fld[0], FieldColor.NO_PLAYER);
			}
		} while( true );
	}
	
}
