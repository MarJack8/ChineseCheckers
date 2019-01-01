package bot;

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
	ArrayList<Field> myGoal;
	Field myCorner;
	
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
					
					for (int y = 0; y < board.HEIGHT; ++y) {
			            for (int x = 0; x < board.WIDTH; ++x) {
			                board.getNode(y, x).setCenterY(100*(y)/(board.HEIGHT-1));
			                board.getNode(y, x).setCenterX(100*((x+1-0.5*(y%2))+2)/board.WIDTH);
			            }
					}
					
					myGoal = board.getWinningFields( FieldColor.values()[ id+1 ] );
					for( Field f: myGoal ) {
						if( f.equals( board.getNode( 1, 6) ) ) { myCorner = f; break; }
						if( f.equals( board.getNode( 5, 0) ) ) { myCorner = f; break; }
						if( f.equals( board.getNode( 5, 12) ) ) { myCorner = f; break; }
						if( f.equals( board.getNode( 13, 0) ) ) { myCorner = f; break; }
						if( f.equals( board.getNode( 13, 12) ) ) { myCorner = f; break; }
						if( f.equals( board.getNode( 17, 6) ) ) { myCorner = f; break; }
					}
				}
				CCMessage msg = readInput();
				if( msg.getSignal().equals( "move" ) ) {
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
					Field from = null;
					Field to = null;
					double mg_min = Double.MAX_VALUE;
					for( Field f: myFields ) {
						ArrayList<Field> legal = select( f.getYCord(), f.getXCord(), board );
						if( legal.isEmpty() ) continue;
						double sg = Math.sqrt( Math.pow(f.getCenterY() - myCorner.getCenterY(), 2) + Math.pow(f.getCenterX() - myCorner.getCenterX(), 2) );
						System.out.println( sg );
						for( Field l: legal ) {
							double mg = Math.sqrt( Math.pow(l.getCenterY() - myCorner.getCenterY(), 2) + Math.pow(l.getCenterX() - myCorner.getCenterX(), 2) );
							if( mg < sg && mg < mg_min ) {
								System.out.println( mg );
								mg_min = mg;
								from = f;
								to = l;
							}
						}
						
					}
					if( from == null || to == null ) {
						System.out.println( "Bot#" + id + ": found no moves" );
						output.add( new CCMessage( "pass" ) );
						continue;
					}
					System.out.println( "Bot#" + id + ": attempting to select (" + from.getYCord() + "," + from.getXCord() + ")" );
					if( !select( from.getYCord(), from.getXCord(), board ).contains( to ) ) {
						System.out.println( "Bot#" + id + ": fatal error" );
						output.add( new CCMessage( "pass" ) );
						continue;
					}
					System.out.println( "Bot#" + id + ": attempting to move to (" + to.getYCord() + "," + to.getXCord() + ")" );
					if( move( to.getYCord(), to.getXCord() ) ) {
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
