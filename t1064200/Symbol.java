import java.util.ArrayList;

public class Symbol {
	private String name;
	private int type;
	private int address;

	// private int memorylocation?

	public Symbol( String name, int type, int address ) {
		this.name = name;
		this.type = type;
		this.address = address;
	}

	public String getName() {
		return this.name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public int getType() {
		return this.type;
	}

	public void setType( int type ) {
		this.type = type;
	}

	public String toString() {
		return this.name + " = " + this.type;
	}

	public int getAddress() {
		return this.address;
	}

	public void setAddress( int address ) {
		this.address = address;
	}
}
