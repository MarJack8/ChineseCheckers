package communication;

import java.io.*;
import java.net.*;

/**
 * Connection should by used to communicate with server
 * @author Tomasz
 *
 */
public class Connection {
	
	Socket socket;
	PrintWriter out;
	BufferedReader in;
	
	/**
	 * Try connecting to server.
	 * If the returned signal is "created", client becomes the game administrator.
	 * Every client should then wait for more messages from server, including {"new_player_joined %id", "start", ...}.
	 * No one but administrator is permitted to send messages to server after connecting (excluding "leave").
	 * More about messages in CCMessage doc file.
	 * 
	 * @param ip Server's IP
	 * @param port Server's PORT
	 * @return CCMessage in {timeout, full, created %id, joined %id}
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public CCMessage connect( String ip, int port ) throws UnknownHostException, IOException {
			// Prepare sockets and i/o
			socket = new Socket( ip, port );
			out = new PrintWriter( socket.getOutputStream(), true );
			in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			
			try {
				// Try to connect to server
				socket.setSoTimeout( 2000 );
				out.println( new CCMessage( "connect" ).toString() );
				// Wait for response
				return CCMessage.fromString( in.readLine() );
			}
			catch( SocketTimeoutException e ) {
				return new CCMessage( "timeout" );
			}
	}
	
	/**
	 * Wait for message from server.
	 * 
	 * @return CCMessage, see doc about exact messages
	 * @throws SocketException
	 * @throws IOException
	 */
	public CCMessage recvSignal() {
		CCMessage signal = null;
		try {
			socket.setSoTimeout( 0 );
			signal = CCMessage.fromString( in.readLine() );
			System.out.println( "Client: " + signal );
		}
		catch( Exception e ) {
			System.out.println( "Client: timeout" );
			return new CCMessage( "timeout" );
		}
		return signal;
	}
	
	/**
	 * Send given CCMessage to server.
	 * Only administrator should use this method. To send "leave" just use close method.
	 * (If other messages are sent by non-administrator, result will be "refuse").
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public CCMessage sendCommand( CCMessage command ) throws IOException {
		socket.setSoTimeout( 5000 );
		out.println( command.toString() );
		try {
			return CCMessage.fromString( in.readLine() );
		}
		catch( Exception e ) {
			return new CCMessage( "timeout" );
		}
	}
	
	/**
	 * Message server about leaving and close connections.
	 * 
	 * @return if closing was successful
	 */
	public boolean close() {
		try {
			out.println( new CCMessage( "leave" ).toString() );
			in.close();
			out.close();
			socket.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	
}
