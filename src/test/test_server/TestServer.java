package test_server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import client.XConnection;
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
	
	class ClientInstance extends Thread {
		
		public int pc;
		
		@Override
		public void run() {
			XConnection xcon = new XConnection();
			try {
				xcon.xconnect( "localhost", 8060 );
				if( xcon.getId() == 1 ) {
					System.out.println( "#" + xcon.getId() + " here. Calling xstart in 1000 ms"  );
					Thread.sleep( 1000 );
					pc = xcon.xstart();
				}
				else {
					System.out.println( "#" + xcon.getId() + " here. Waiting for game to start"  );
					pc = xcon.xwaitForGameStart();
				}
				Thread.sleep( 1000 );
				xcon.close();
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
	public void testStartingGame() throws Exception {
		ClientInstance c1 = new ClientInstance();
		ClientInstance c2 = new ClientInstance();
		c1.start();
		c2.start();
		c1.join();
		c2.join();
		assertEquals( 2, c1.pc );
		assertEquals( 2, c2.pc );
	}
	
}
