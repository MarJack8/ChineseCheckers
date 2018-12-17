package communication;

import java.io.IOException;

public abstract class Player extends Thread {
	
	public final int id;
	public GameMaster gm;
	
	public boolean win;
	
	Player( int _id ) {
		win = false;
		id = _id;
	}
	
	@Override
	abstract public void run();
	
	abstract public void send( CCMessage msg );
	abstract public void permit();
	abstract public CCMessage recv();
	abstract public void halt();
	
	public void setWin() {
		win = true;
	}
	
	public boolean getWin() {
		return win;
	}
	
	abstract public void close() throws IOException;
	
}
