package communication;

import java.util.ArrayList;

public class CCMessage {
	String signal;
	ArrayList<Integer> args;
	
	public CCMessage( String _signal ) {
		signal = _signal;
		args = new ArrayList<Integer>();
	}
	
	public void insertArg( int x ) {
		args.add( x );
	}
	
	public String getSignal() {
		return signal;
	}
	
	public ArrayList<Integer> getArgs() {
		return args;
	}
	
	public int getArg( int x ) {
		return args.get( x );
	}
	
	public String toString() {
		String ret = signal;
		for( Integer i: args ) {
			ret += " ";
			ret += i;
		}
		return ret;
	}
	
	public static CCMessage fromString( String src ) {
		String[] words = src.split( " " );
		if( words.length < 1 ) return null;
		CCMessage ret = new CCMessage( words[0] );
		for( int i = 1; i<words.length; i++ ) {
			try {
				ret.insertArg( Integer.parseInt( words[i] ) );
			}
			catch( NumberFormatException e ) {
				return null;
			}
		}
		return ret;
	}
}
