package test_communication;

import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import communication.CCMessage;

public class TestCCMessage {
	
	@Test
	public void test_fromString() {
		CCMessage tm = CCMessage.fromString( "hello 1 2 3" );
		assertEquals( "hello", tm.getSignal() );
		ArrayList<Integer> args = tm.getArgs();
		assertEquals( (int) args.get( 1 ), 2 );
	}
	
	@Test
	public void test_toString() {
		CCMessage tm = new CCMessage( "hello" );
		tm.insertArg( 10 );
		tm.insertArg( 12 );
		assertEquals( tm.toString(), "hello 10 12" );
	}
	
}
