package client;

import communication.CCMessage;
import communication.Connection;
import game.Board;
import game.Field;
import game.FieldColor;

import java.io.IOException;
import java.util.ArrayList;

public class XConnection extends Connection {

	int id;
	CCMessage conmsg;

	public void xconnect( String ip, int port ) throws IOException {
		conmsg = connect( ip, port );
		id = conmsg.getArg( 0 );
	}

	public CCMessage getConnectionMessage() {
		return conmsg;
	}

	public int getId() {
		return id;
	}

	/**
	 * Request to select given field.
	 * Board is needed to return proper set.
	 * @param y
	 * @param x
	 * @param board
	 * @return Legal moves, or null if request was illegal
	 * @throws IOException on connection loss
	 */
	public ArrayList<Field> xselect( int y, int x, Board board ) throws IOException {
		CCMessage cm = new CCMessage( "select" );
		cm.insertArg( y );
		cm.insertArg( x );
		CCMessage sm = sendCommand( cm );
		if( sm.getSignal().equals( "legal" ) ) {
			ArrayList<Field> ret = new ArrayList<Field>();
			int o = 0;
			int iy = 0;
			ArrayList<Integer> args = sm.getArgs();
			for( Integer i: args ) {
				if( o == 0 ) {
					iy = i;
					o++;
				}
				else {
					ret.add( board.getNode( iy, i ) );
					o--;
				}
			}
			return ret;
		}
		return new ArrayList<Field>();
	}

	/**
	 * Attempt to move PREVIOUSLY SELECTED (using xselect) pawn to given field.
	 * @param y
	 * @param x
	 * @return if server accepted this move
	 * @throws IOException on connection loss
	 */
	public boolean xmove( int y, int x ) throws IOException {
		CCMessage cm = new CCMessage( "move" );
		cm.insertArg( y );
		cm.insertArg( x );
		CCMessage sm = sendCommand( cm );
		return sm.getSignal() == "success";
	}

	/**
	 * Pass.
	 * @return should always return true, may be false send without permission.
	 * @throws IOException on connection loss
	 */
	public boolean xpass() throws IOException {
		CCMessage sm = sendCommand( new CCMessage( "pass" ) );
		return sm.getSignal().equals("success");
	}

	/**
	 * Translates CCMessage "move" to Fields on board.
	 * @param msg
	 * @param board
	 * @return Field[2] where [0] is "from" and [1] is "to".
	 */
	public Field[] xgetMove( CCMessage msg, Board board ) {
		Field[] ret = new Field[2];
		ret[0] = board.getNode( msg.getArgs().get( 0 ) , msg.getArgs().get( 1 ) );
		ret[1] = board.getNode( msg.getArgs().get( 2 ) , msg.getArgs().get( 3 ) );
		return ret;
	}

	/**
	 * Translates CCMessage "move" to FieldColor.
	 * @param msg
	 * @return FieldColor
	 */
	public FieldColor xgetColor( CCMessage msg ) {
		return FieldColor.values()[msg.getArg( 4 )];
	}

	/**
	 * Wait for game to start.
	 * @return player count, 0 if game not started
	 */
	public int xwaitForGameStart() {
		CCMessage sm = recvSignal();
		if( sm.getSignal().equals( "start_success" ) ) {
			return sm.getArg( 0 );
		}
		else return 0;
	}

	/**
	 * Attempt to start the game
	 * @return player count, 0 if game not started
	 * @throws IOException
	 */
	public int xstart() throws IOException {
		CCMessage sm = sendCommand( new CCMessage( "start" ) );
		if( sm.getSignal().equals( "start_success" ) ) {
			return sm.getArg( 0 );
		}
		else return 0;
	}

	public CCMessage xaddBot() throws IOException {
		return sendCommand( new CCMessage( "add_bot" ) );
	}
}
