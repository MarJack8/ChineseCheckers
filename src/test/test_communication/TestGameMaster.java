package test_communication;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import communication.CCMessage;
import communication.Connection;
import communication.GameMaster;

class TestGameMaster {
	
	final String ip = "localhost";
	
	class SomeServer extends Thread {
		public GameMaster gm;
		public int port = 6666;
		@Override
		public void run() {
			try {
				gm = new GameMaster( port );
				gm.waitForClients();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	class SomeClient extends Thread {
		
		Queue<CCMessage> received;
		boolean end;
		
		Connection client;
		SomeClient( String ip, int port ) throws UnknownHostException, IOException {
			client = new Connection();
			received = new LinkedList<CCMessage>();
			client.connect( ip, port );
			start();
		}
		
		@Override
		public void run() {
			try {
				CCMessage ret = client.sendCommand( new CCMessage( "start" ) );
				if( !ret.toString().equals( "start_success" ) ) {
					System.out.println( "Test: game not started" );
					return;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			while( true ) {
				try {
					System.out.println( "Test: recvSignal" );
					CCMessage msg = client.recvSignal();
					received.add( msg );
					if( msg.getSignal().equals( "end" ) ) {
						System.out.println( "Test: client received 'end'" );
						break;
					}
					if( msg.getSignal().equals( "ping" ) ) {
						CCMessage ret = client.sendCommand( new CCMessage( "pong" ) );
						System.out.println( "Test: client received '" + ret.toString() + "'" );
					}
				} catch( Exception e ) {
					e.printStackTrace();
					break;
				}
			}
			client.close();
		}
		
		public CCMessage recv() {
			while( received.isEmpty() );
			return received.poll();
		}
	}

	@Test
	void test_acceptClient() {
		try {
			SomeServer server = new SomeServer();
			server.port = 6666;
			server.start();
			Connection client0 = new Connection();
			CCMessage msg = client0.connect( ip, server.port );
			assertEquals( "created 0", msg.toString() );
			assertEquals( 1, server.gm.getPlayerCount() );
			client0.close();
		} catch( Exception e ) {
			fail( e.getMessage() );
		}
	}
	
	@Test
	void test_removeClient() {
		try {
			SomeServer server = new SomeServer();
			server.port = 6667;
			server.start();
			Connection client0 = new Connection();
			Connection client1 = new Connection();
			client0.connect( ip, server.port );
			client1.connect( ip, server.port );
			client1.close();
			Thread.sleep( 100 ); // give server some time
			assertEquals( 1, server.gm.getPlayerCount() );
			client0.close();
			Thread.sleep( 100 );
			assertEquals( true, server.gm.game_cancelled );
		} catch( Exception e ) {
			fail( e.getMessage() );
		}
	}
	
	@Test
	void test_StartingGame() {
		try {
			SomeServer server = new SomeServer();
			server.port = 6668;
			server.start();
			Connection client0 = new Connection();
			client0.connect( ip, server.port );
			CCMessage msg = client0.sendCommand( new CCMessage( "start" ) );
			Thread.sleep( 100 );
			assertEquals( true, server.gm.game_started );
			assertEquals( "start_success", msg.toString() );
			System.out.println( "Test: closing" );
			server.gm.close();
			client0.close();
		} catch( Exception e ) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}
	
	@Test
	void test_sendToPlayer() {
		try {
			SomeServer server = new SomeServer();
			server.port = 6669;
			server.start();
			SomeClient client = new SomeClient( ip, server.port );
			Thread.sleep( 100 );
			server.gm.sendToPlayer( 0, new CCMessage( "end" ) );
			assertEquals( "end", client.recv().toString() );
			System.out.println( "Test: closing" );
			server.gm.close();
		} catch( Exception e ) {
			fail( e.getMessage() );
		}
	}
	
	@Test
	void test_recvFromPlayer() {
		try {
			SomeServer server = new SomeServer();
			server.port = 6660;
			server.start();
			new SomeClient( ip, server.port );
			Thread.sleep( 100 );
			server.gm.permitPlayer( 0 );
			server.gm.sendToPlayer( 0, new CCMessage( "ping" ) );
			CCMessage res = server.gm.recvFromPlayer( 0 );
			server.gm.haltPlayer( 0 );
			assertEquals( "pong", res.toString() );
			Thread.sleep( 100 );
			server.gm.sendToPlayer( 0, new CCMessage( "thanks" ) );
			Thread.sleep( 100 );
			server.gm.sendToPlayer( 0, new CCMessage( "end" ) );
			System.out.println( "Test: closing" );
			server.gm.close();
		} catch( Exception e ) {
			fail( e.getMessage() );
		}
	}
}
