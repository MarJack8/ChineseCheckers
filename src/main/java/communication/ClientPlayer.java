package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class ClientPlayer extends Player {
	
	Socket client;
	PrintWriter out;
	BufferedReader in;

	boolean permitted;
	Queue<CCMessage> received;
	
	ClientPlayer( int _id, Socket _client, PrintWriter _out, BufferedReader _in ) {
		super( _id );
		client = _client;
		out = _out;
		in = _in;
		permitted = false;
		received = new LinkedList<CCMessage>();
	}
	
	CCMessage readLine() throws SocketException, InterruptedException, ContinueException {
		client.setSoTimeout( 100 );
		while( true ) {
			try {
				String x = in.readLine();
				return CCMessage.fromString( x );
			} catch( Exception e ) {
				if( interrupted() || gm.game_cancelled ) throw new InterruptedException();
				if( gm.game_started ) throw new ContinueException();
			}
		}
	}
	
	@Override
	public void run() {
		CCMessage res;
		if( id == 0 ) res = new CCMessage( "created" );
		else res = new CCMessage( "joined" );
		res.insertArg( id+1 );
		send( res );
		while( !gm.game_cancelled ) {
			try {
				CCMessage msg = readLine();
				System.out.println( "Received '" + msg.getSignal() + "' signal from #" + id );
				if( !gm.game_started ) { // WAITING FOR MORE PLAYERS
					if( msg.getSignal().equals( "leave" ) ) { // Leave
						gm.removeClient( this );
						closeConnections();
						return;
					}
					if( id == 0 ) { // Administrator
						if( msg.getSignal().equals( "start" ) ) { // Start
							if( gm.getPlayerCount() == 1 || gm.getPlayerCount() == 5 ) {
								System.out.println( "Attempt to start game failed due to incorrect player count" );
								send( new CCMessage( "failure" ) );
							}
							else {
								System.out.println( "Starting game" );
								gm.start();
							}
						}
					}
				}
				else { // DURING GAME
					// Free signals
					if( msg.getSignal().equals( "leave" ) ) {
						// TODO
						// Surrender?
						System.out.println( "### 'leave' signal not implemented" );
						continue;
					}
					// Signals that require permission
					if( permitted ) {
						received.add( msg );
						System.out.println( "#" + id + " message registered" );
					}
					else {
						System.out.println( "#" + id + " not permitted" );
						send( new CCMessage( "refuse" ) );
					}
				}
			} catch( InterruptedException e ) {
				System.out.println( "Interrupting #" + id );
				return;
			} catch( ContinueException e ) {
				continue;
			} catch( Exception e ) {
				System.out.println( "Exception in #" + id + ": " + e.getMessage() );
				try {
					Thread.sleep( 1000 );
				} catch (InterruptedException e1) {
					System.out.println( "Interrupting #" + id );
					return;
				}
				continue;
			}
		}
	}

	@Override
	public void permit() {
		permitted = true;
		System.out.println( "Player #" + id + " permitted" );
	}
	
	@Override
	public CCMessage recv() {
		if( !permitted ) return new CCMessage( "not_permitted" );
		while( received.isEmpty() );
		return received.poll();
	}
	
	@Override
	public void halt() {
		permitted = false;
		received.clear();
		System.out.println( "Player #" + id + " halted" );
	}
	
	@Override
	public void send( CCMessage msg ) {
		out.println( msg.toString() );
	}
	
	void closeConnections() throws IOException {
		out.close();
		in.close();
		client.close();
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
		closeConnections();
	}
}
