package client;

import communication.CCMessage;
import game.Board;
import game.Field;
import game.FieldColor;

public class Listener extends Thread {
	
	XConnection xcon;
	Board board;
	boolean myturn;
	
	Listener( Board b, XConnection c ) {
		xcon = c;
		board = b;
		myturn = false;
	}
	
	public boolean isItMyTurn() {
		return myturn;
	}
	
	@Override
	public void run() {
		CCMessage msg;
		do {
			msg = xcon.recvSignal();
			if( msg.getSignal().equals( "your_turn" ) ) {
				myturn = true;
				break;
			}
			if( msg.getSignal().equals( "move" ) ) {
				Field[] fld = xcon.xgetMove(msg, board);
	            FieldColor clr = xcon.xgetColor(msg);
	            board.changeFieldColor(fld[1], clr);
	            board.changeFieldColor(fld[0], FieldColor.NO_PLAYER);
			}
		} while( true );
	}
	
}
