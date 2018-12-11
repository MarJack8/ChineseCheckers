package communication;

import java.io.IOException;

public abstract class Player extends Thread {
	
	public final int id;
	public GameMaster gm;
	
	Player( int _id ) {
		id = _id;
	}
	
	@Override
	abstract public void run();
	
	abstract public void send( CCMessage msg );
	abstract public void permit();
	abstract public CCMessage recv();
	abstract public void halt();
	
	abstract public void close() throws IOException;
	
}
