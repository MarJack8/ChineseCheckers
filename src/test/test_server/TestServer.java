package test_server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import client.XConnection;
import communication.CCMessage;
import game.Board;
import game.Field;
import game.FieldColor;
import server.Server;

class TestServer {
	static ServerInstance si;
	static class ServerInstance extends Thread {
		@Override
		public void run() {
			try {
				Server.main( new String[0] );
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	class ClientInstance implements Runnable {
		
		XConnection xcon;
		public int id;
		public int pc;
		Board board;
		
		int stage = 0;
		
		ClientInstance() {
			xcon = new XConnection();
			id = -1;
		}
		
		public void xstart() throws Exception {
			pc = xcon.xstart();
			if( pc > 1 ) {
				stage++;
			}
		}
		
		@Override
		public void run() {
			try {
				if( stage == 0 ) {
					xcon.xconnect( "localhost", 8060 );
					id = xcon.getId();
					if( id != 1 ) {
						pc = xcon.xwaitForGameStart();
						if( pc > 1 ) {
							stage++;
						}
					}
				}
				if( stage == 1 ) {
					board = new Board( pc );
					for( int i = 0; i<1; i++ ) {
						CCMessage msg = xcon.recvSignal();
						if( msg.getSignal().equals( "your_turn" ) ) {
							ArrayList<Field> myFields = board.getFieldsByColor( FieldColor.values()[ id ] );
							if( myFields.isEmpty() ) fail( "No my fields" );
							ArrayList<Field> legal = new ArrayList<>();
							for( Field f: myFields ) {
								legal = xcon.xselect( f.getYCord(), f.getXCord(), board );
							}
							if( legal.isEmpty() ) fail( "No legal moves" );
							if( xcon.xmove( legal.get( 0 ).getYCord(), legal.get( 0 ).getYCord() ) ) {
								
							}
						}
					}
					xcon.close();
				}
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	@BeforeAll
	static public void runServer() {
		System.out.println( "Starting ServerInstance" );
		TestServer.si = new ServerInstance();
		si.start();
	}
	
	@Test
	public void testGame() throws Exception {
		ClientInstance c1 = new ClientInstance();
		ClientInstance c2 = new ClientInstance();
		Thread t1 = new Thread( c1 );
		Thread t2 = new Thread( c2 );
		t1.start();
		Thread.sleep( 100 );
		t2.start();
		t1.join();
		assertEquals( 1, c1.id );
		c1.xstart();
		t1 = new Thread( c1 );
		t1.start();
		t1.join();
		t2.join();
		assertEquals( 2, c1.pc );
		assertEquals( 2, c2.pc );
	}
	
}
