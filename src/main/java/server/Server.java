package server;

import communication.CCMessage;
import communication.GameMaster;
import game.Board;
import game.Field;
import game.FieldColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Server {
	
	public static void main( String[] args ) throws IOException {
		int port = 8060;
		if( args.length > 1 ) {
			port = Integer.parseInt( args[0] );
		}
		System.out.println( "Starting at " + port );
		GameMaster gm = new GameMaster( port );
		if( gm.waitForClients() ) {
			// In this point server has already sent "start_success" to all players
			Board board = new Board( gm.getPlayerCount() );
			for( int currentPlayer = (new Random()).nextInt( gm.getPlayerCount() ); !gm.gameFinished(); currentPlayer = ( currentPlayer + 1 ) % gm.getPlayerCount() ) {
				if( !gm.getWin( currentPlayer ) ) {
					gm.permitPlayer( currentPlayer );
					gm.sendToPlayer( currentPlayer, new CCMessage( "your_turn" ) );
					
					System.out.println( "#" + currentPlayer + "'s turn" );
					
					Field selected = null;
					ArrayList<Field> legal = null;
					
					while( true ) {
						CCMessage pm = gm.recvFromPlayer( currentPlayer );
						if( pm.getSignal().equals( "pass" ) ) {
							gm.sendToPlayer( currentPlayer, new CCMessage( "success" ) );
							System.out.println( "#" + currentPlayer + " passed" );
							break;
						}
						else if( pm.getSignal().equals( "select" ) ) {
							ArrayList<Integer> ag = pm.getArgs();
							selected = board.getNode( ag.get( 0 ), ag.get( 1 ) );
							if( selected.getFieldColor() == FieldColor.values()[ currentPlayer + 1 ] ) {
								legal = board.getLegal( selected );
								CCMessage sm = new CCMessage( "legal" );
								for( Field f: legal ) {
									sm.insertArg( f.getYCord() );
									sm.insertArg( f.getXCord() );
								}
								System.out.println( "Accepted #" + currentPlayer + " selection. There are " + legal.size() + " moves" );
								gm.sendToPlayer( currentPlayer, sm );
							}
							else {
								gm.sendToPlayer( currentPlayer, new CCMessage( "illegal" ) );
							}
						}
						else if( pm.getSignal().equals( "move" ) ) {
							if( selected != null && legal != null ) {
								ArrayList<Integer> ag = pm.getArgs();
								Field destination = board.getNode( ag.get( 0 ), ag.get( 1 ) );
								if( legal.contains( destination ) ) {
									gm.sendToPlayer( currentPlayer, new CCMessage( "success" ) );
									CCMessage sm = new CCMessage( "move" );
									sm.insertArg( selected.getYCord() );
									sm.insertArg( selected.getXCord() );
									sm.insertArg( destination.getYCord() );
									sm.insertArg( destination.getXCord() );
									sm.insertArg( currentPlayer + 1 );
									gm.sendToAll( sm );
									System.out.println( "#" + currentPlayer + " moved from (" + selected.getYCord() + "," + selected.getXCord() + ") to (" + destination.getYCord() + "," + destination.getXCord() + ")" );
									destination.setColor( FieldColor.values()[ currentPlayer + 1 ] );
									selected.setColor( FieldColor.NO_PLAYER );
									break;
								}
							}
							gm.sendToPlayer( currentPlayer, new CCMessage( "illegal" ) );
						}
						else {
							// Bad usage on server side
							gm.sendToPlayer( currentPlayer, new CCMessage( "no?" ) );
						}
					}
					gm.haltPlayer( currentPlayer );
					
					// TODO
					if( false /*currentPlayer has won*/ ) {
						gm.sendToPlayer( currentPlayer, new CCMessage( "victory" ) );
						gm.setWin( currentPlayer );
					}
				}
			}
			gm.sendToAll( new CCMessage( "finished" ) );
		}
		else {
			gm.sendToAll( new CCMessage( "canceled" ) );
			System.out.println( "Game not started" );
		}
		gm.close();
	}

}
