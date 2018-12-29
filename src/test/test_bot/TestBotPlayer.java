package test_bot;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import communication.CCMessage;
import communication.GameMaster;

class TestBotPlayer {

	@Test
	void testSend() throws IOException {
		GameMaster gm = new GameMaster( 0 );
		int bot = gm.simulateBot();
		gm.permitPlayer( bot );
		gm.sendToPlayer( bot, new CCMessage( "ping" ) );
		CCMessage msg = gm.recvFromPlayer( bot );
		assertEquals( "pong", msg.getSignal() );
		gm.haltPlayer( bot );
	}

}
