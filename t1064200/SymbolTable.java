import java.util.HashMap;
import java.lang.Exception;

public class SymbolTable {
	private HashMap<String, Symbol> table;

	public SymbolTable() {
		this.table = new HashMap<String, Symbol>();
	}

	public class SymbolNotFoundException extends Exception {

		public SymbolNotFoundException( String message ) {
			// TODO Auto-generated constructor stub
			super(message);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 2948507857579726139L;

	}

	public void add( String name, int type, int address ) {
		Symbol toAdd = new Symbol(name, type, address);
		// Allocate address for symbol
		this.table.put(name, toAdd);
	}

	public boolean exists( String name ) {
		return this.table.containsKey(name);
	}

	public int typeOf( String name ) {
		if ( this.table.containsKey(name) )
			return this.table.get(name).getType();
		else
			return -1;
	}

	public int addressOf( String name ) {
		if ( this.table.containsKey(name) )
			return this.table.get(name).getAddress();
		else
			return -1;
	}

	public boolean sameType( String symbolOne, String symbolTwo )
			throws SymbolNotFoundException {
		int first = this.typeOf(symbolOne);
		int second = this.typeOf(symbolTwo);
		return first == second;
	}

	public String toString() {
		return this.table.toString();
	}
}
