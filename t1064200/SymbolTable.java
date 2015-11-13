import java.util.HashMap;
import java.lang.Exception;

public class SymbolTable {
	private HashMap<String, Symbol> table;
	private int framesize;

	public SymbolTable() {
		this.table = new HashMap<String, Symbol>();
		this.framesize = 0;
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

	public Symbol add( String name, int type ) {
		Symbol toAdd = new Symbol(name, type);
		// Allocate address for symbol
		this.framesize++;
		toAdd.address = this.framesize;
		this.table.put(name, toAdd);
		return toAdd;
	}
	
	public Symbol get(String name) throws SymbolNotFoundException {
		if (!this.table.containsKey(name)) {
			throw new SymbolNotFoundException("Symbol " + name + " not declared");
		}
		return this.table.get(name);
	}

	public boolean exists( String name ) {
		return this.table.containsKey(name);
	}

	public int typeOf( String name ) {
		if ( this.table.containsKey(name) )
			return this.table.get(name).type;
		else
			return -1;
	}

	public int addressOf( String name ) {
		if ( this.table.containsKey(name) )
			return this.table.get(name).address;
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

	public class Symbol {
	    public String name;
	    public int type;
	    public int address;
	
	    public Symbol(String name, int type) {
	        this.name = name;
	        this.type = type;
	    }
	}
}
