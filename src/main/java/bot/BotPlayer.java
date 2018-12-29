package bot;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import communication.CCMessage;
import communication.Player;
import game.Board;
import game.Field;
import game.FieldColor;

public class BotPlayer extends Player {
	
	Board board;
	
	boolean permitted;
	Queue<CCMessage> input;
	Queue<CCMessage> output;
	
	public BotPlayer( int _id ) {
		super(_id);
		input = new LinkedList<CCMessage>();
		output = new LinkedList<CCMessage>();
	}
	
	CCMessage readInput() throws InterruptedException {
		while( input.isEmpty() ) {
			Thread.sleep( 100 );
		}
		return input.poll();
	}

	@Override
	public void run() {
		while( !gm.game_cancelled ) {
			try {
				if( !gm.game_started ) {
					while( !gm.game_started ) Thread.sleep( 100 );
					board = new Board( gm.getPlayerCount() );
				}
				CCMessage msg = readInput();
				if( msg.getSignal().equals( "move" ) ) {
					System.out.println( msg );
					Field[] fld = getMove( msg, board );
		            FieldColor clr = getColor( msg );
		            board.changeFieldColor( fld[1], clr );
		            board.changeFieldColor( fld[0], FieldColor.NO_PLAYER );
				}
				else if( msg.getSignal().equals( "finished" ) ) {
					break;
				}
				else if( msg.getSignal().equals( "your_turn" ) ) {
					ArrayList<Field> myFields = board.getFieldsByColor( FieldColor.values()[ id+1 ] );
					if( myFields.isEmpty() ) {
						System.out.println( "No my fields!!!" );
						output.add( new CCMessage( "pass" ) );
						continue;
					}
					ArrayList<Field> legal = new ArrayList<>();
					for( Field f: myFields ) {
						System.out.println( "Bot#" + id + ": attempting to select (" + f.getYCord() + "," + f.getXCord() + ")" );
						legal = select( f.getYCord(), f.getXCord(), board );
						if( !legal.isEmpty() ) break;
					}
					if( legal.isEmpty() ) {
						System.out.println( "Bot#" + id + ": found no legal moves" );
						output.add( new CCMessage( "pass" ) );
						continue;
					}
					System.out.println( "Bot#" + id + ": attempting to move to (" + legal.get( 0 ).getYCord() + "," + legal.get( 0 ).getXCord() + ")" );
					if( move( legal.get( 0 ).getYCord(), legal.get( 0 ).getXCord() ) ) {
						continue;
					}
					else {
						System.out.println( "Legal move is illegal!!!" );
						output.add( new CCMessage( "pass" ) );
						continue;
					}
				}
				else if( msg.getSignal().equals( "ping" ) ) {
					output.add( new CCMessage( "pong" ) );
				}
				else if( msg.getSignal().equals( "victory" ) ) {
					System.out.println( "Bot#" + id + ": gg (" + msg.getArg( 0 ) + " place)" );
				}
			}
			catch( InterruptedException e ) {
				System.out.println( "Interrupting #" + id );
				return;
			}
			catch( NullPointerException e ) {
				System.out.println( "Bot received null" );
				e.printStackTrace();
				try {
					Thread.sleep( 10000 );
				} catch (InterruptedException e1) {
					System.out.println( "Interrupting #" + id );
					return;
				}
				continue;
			}
		}
	}

	@Override
	public void send( CCMessage msg ) {
		input.add( msg );
	}

	@Override
	public void permit() {
		System.out.println( "Bot #" + id + " permitted" );
		permitted = true;
	}

	@Override
	public CCMessage recv() {
		if( !permitted ) return new CCMessage( "not_permitted" );
		while( output.isEmpty() )
			try {
				Thread.sleep( 100 );
				if( getDcd() ) return new CCMessage( "disconnected" );
			} catch (InterruptedException e) {
				e.printStackTrace();
			};
		return output.poll();
	}

	@Override
	public void halt() {
		permitted = false;
		output.clear();
		System.out.println( "Bot #" + id + " halted" );
	}

	@Override
	public void close() throws IOException {
		interrupt();
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println( "Closing #" + id + " ends in thread alive=" + isAlive() );
	}

	Field[] getMove( CCMessage msg, Board board ) {
		Field[] ret = new Field[2];
		ret[0] = board.getNode( msg.getArgs().get( 0 ) , msg.getArgs().get( 1 ) );
		ret[1] = board.getNode( msg.getArgs().get( 2 ) , msg.getArgs().get( 3 ) );
		return ret;
	}

	FieldColor getColor( CCMessage msg ) {
		return FieldColor.values()[ msg.getArg( 4 ) ];
	}
	
	ArrayList<Field> select( int y, int x, Board board ) throws InterruptedException {
		CCMessage cm = new CCMessage( "select" );
		cm.insertArg( y );
		cm.insertArg( x );
		output.add( cm );
		CCMessage sm = readInput();
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
		else {
			System.out.println( "Bot#" + id + ": illegal selection?" );
		}
		return new ArrayList<Field>();
	}
	
	public boolean move( int y, int x ) throws InterruptedException {
		CCMessage cm = new CCMessage( "move" );
		cm.insertArg( y );
		cm.insertArg( x );
		output.add( cm );
		CCMessage sm = readInput();
		return sm.getSignal().equals("success");
	}
	
}
