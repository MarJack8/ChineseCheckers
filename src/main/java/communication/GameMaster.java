package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Server side of connection
 * @author Tomasz
 *
 */
public class GameMaster {
	
	final int port;
	
	ServerSocket server;
	Player[] players;
	Integer playerCount;
	public boolean game_started;
	public boolean game_cancelled;
	
	public GameMaster( int _port ) throws IOException {
		port = _port;
		server = new ServerSocket( _port );
		players = new Player[6];
		playerCount = 0;
		game_started = false;
		game_cancelled = false;
	}
	
	public void close() throws IOException {
		System.out.println( "Closing GameMaster" );
		game_cancelled = true;
		for( int i = 0; i<playerCount; i++ ) {
			players[i].close();
		}
		server.close();
		System.out.println( "GameMaster has closed" );
	}
	
	/**
	 * Waits for new connections and starts a thread for them.
	 * Exits when game administrator sends "start".
	 * @throws IOException 
	 * @return If game has successfully started
	 */
	public boolean waitForClients() throws IOException {
		System.out.println( "Waiting for connections..." );
		while( !game_cancelled && !game_started ) {
			try {
				acceptClient();
			} catch( Exception e ) {
				break;
			}
			System.out.println( "Players [" + playerCount + "/6]" );
		}
		System.out.println( "Waiting for connections has ended" );
		if( game_cancelled ) close();
		else server.setSoTimeout( 0 );
		if( game_started ) {
			System.out.println( "Game successfully started" );
			CCMessage msg = new CCMessage( "start_success" );
			msg.insertArg( getPlayerCount() );
			sendToAll( msg );
		}
		
		return game_started;
	}

	void acceptClient() throws Exception {
		Socket client = server.accept();
		PrintWriter out = new PrintWriter( client.getOutputStream(), true );
        BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
        // Leaving space to possible reconnect implementation
        CCMessage signal = CCMessage.fromString( in.readLine() );
        if( signal.getSignal().equals( "start" ) ) {
        	out.close();
        	in.close();
        	client.close();
        	throw new Exception();
        }
        System.out.println( "Received '" + signal.getSignal() + "' signal" );
        if( signal.getSignal().equals( "connect" ) ) {
        	synchronized( playerCount ) {
        		if( playerCount >= 6 ) {
        			out.println( new CCMessage( "full" ).toString() );
        			System.out.println( "Refused new client, game full" );
        		}
            	else {
            		ClientPlayer cp = new ClientPlayer( playerCount, client, out, in );
            		cp.gm = this;
            		players[playerCount] = cp;
            		cp.start();
            		playerCount++;
            		System.out.println( "Accepted new client #" + cp.id );
            	}
        	}
        }
        else {
        	out.println( new CCMessage( "refuse" ).toString() );
        	System.out.println( "Refused new client, invalid request" );
        }
	}
	
	/**
	 * 
	 * @param p Player to remove
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void removeClient( Player p ) throws IOException, InterruptedException {
		if( p.id == 0 ) {
			System.out.println( "Player #0 left, cancelling game" );
			game_cancelled = true;
			server.close();
			return;
		}
		synchronized( playerCount ) {
			players[p.id] = null;
			for( int i = p.id; i<playerCount; i++ ) {
				if( i+1 < playerCount ) players[i] = players[i+1];
			}
			playerCount--;
			System.out.println( "Removed player #" + p.id );
			System.out.println( "Players [" + playerCount + "/6]" );
		}
	}
	
	public int getPlayerCount() {
		return playerCount;
	}
	
	public ArrayList<Integer> getPlayers() {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for( int i = 0; i<playerCount; i++ ) {
			ret.add( players[i].id );
		}
		return ret;
	}
	
	/**
	 * Sends message to all connected players
	 * @param msg
	 */
	public void sendToAll( CCMessage msg ) {
		for( int i = 0; i<playerCount; i++ ) {
			sendToPlayer( i, msg );
		}
	}
	
	/**
	 * Sends message to player given by id
	 * @param i player id
	 * @param msg
	 * @throws NullPointerException when there is no such player
	 */
	public void sendToPlayer( int i, CCMessage msg ) throws NullPointerException {
		players[i].send( msg );
	}
	
	public void permitPlayer( int i ) throws NullPointerException {
		players[i].permit();
	}
	
	/**
	 * Waits for new messages and returns first one from queue.
	 * Server can received more messages from this client by calling this method multiple times.
	 * Call permitPlayer before this method.
	 * Afterwards, haltPlayer should be called to stop saving messages in queue.
	 * @param i player is
	 * @return the message
	 * @throws NullPointerException when there is no such player
	 */
	public CCMessage recvFromPlayer( int i ) throws NullPointerException {
		return players[i].recv();
	}
	
	/**
	 * Stops receiving messages (ignore them),
	 * and cleans current queue.
	 * @param i player id
	 * @throws NullPointerException when there is no such player
	 */
	public void haltPlayer( int i ) throws NullPointerException {
		players[i].halt();
	}
	
	public void setWin( int i ) {
		players[i].setWin();
	}
	
	public boolean getWin( int i ) {
		return players[i].getWin();
	}

	public void start() throws ContinueException, IOException {
		game_started = true;
		Socket dummy = new Socket( "localhost", port );
		PrintWriter out = new PrintWriter( dummy.getOutputStream(), true );
		out.println( "start" );
		out.close();
		dummy.close();
	}

	public boolean gameFinished() {
		int win = 0;
		for( int i = 0; i<playerCount; i++ ) {
			if( getWin( i ) ) win++;
		}
		return win >= getPlayerCount()-1;
	}
}
