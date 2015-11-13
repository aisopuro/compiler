

import java.util.Deque;
import java.util.ArrayDeque;
import java.lang.Integer;



public class Parser {
	public static final int _EOF = 0;
	public static final int _identifier = 1;
	public static final int _integer = 2;
	public static final int maxT = 27;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	private Printer printer;

private SymbolTable table = new SymbolTable();
private CodeGenerator generator = new CodeGenerator();


// Token types
private static final int UNDEFINED = 0;
private static final int INTEGER = UNDEFINED + 1;
private static final int BOOLEAN = INTEGER + 1;

// @SLX

private String id;

private String out;

private int checkInteger(String value) {
	int val = 0;
	try {
		val = Integer.valueOf(value);
	}
	catch (NumberFormatException e) {
		// Number is too large
		this.SemErr("overflow: integer is too large");
	}
	return val;
}

public Parser(Scanner s, Printer p) {
       this(s);
       this.printer = p;
}

private class OpData {
	public String cw;
	public int expected;
	public int result;
}



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void Grammar() {
		printer.startProduction("Grammar");
		
		MainFuncDecl();
		Expect(0);
		printer.print("Table: " + table.toString());
		this.generator.halt();
		printer.endProduction("Grammar");
		
	}

	void MainFuncDecl() {
		printer.startProduction("MainFuncDecl"); 
		Expect(3);
		
		FuncBody();
		printer.endProduction("MainFuncDecl"); 
	}

	void FuncBody() {
		printer.startProduction("FuncBody"); 
		Expect(4);
		
		VarDecl();
		StatementList();
		ReturnStatement();
		Expect(5);
		printer.endProduction("FuncBody");
		
	}

	void VarDecl() {
		int type;
		printer.startProduction("VarDecl"); 
		if (la.kind == 20 || la.kind == 21) {
			type = Type();
			if (type == UNDEFINED) SemErr("Incorrect type declaration");
			Expect(1);
			String name = t.val;
			if (this.table.exists(name)) {
			SemErr("Double variable definition, " + name + " already exists");
			}
			else {
			SymbolTable.Symbol symbol = this.table.add(name, type);
			}
			
			Expect(6);
			VarDecl();
			printer.endProduction("VarDecl"); 
		} else if (StartOf(1)) {
			printer.print("_");
			printer.endProduction("VarDecl");
			
		} else SynErr(28);
	}

	void StatementList() {
		printer.startProduction("StatementList"); 
		if (StartOf(2)) {
			Statement();
			StatementList();
			printer.endProduction("StatementList"); 
		} else if (la.kind == 5 || la.kind == 7) {
			printer.endProduction("StatementList"); 
		} else SynErr(29);
	}

	void ReturnStatement() {
		int type; SymbolTable.Symbol symbol; printer.startProduction("ReturnStatement"); 
		Expect(7);
		printer.print("return"); 
		type = Expr();
		Expect(6);
		printer.endProduction("ReturnStatement"); 
	}

	int  Type() {
		int  type;
		type = UNDEFINED;
		printer.startProduction("Type");
		
		if (la.kind == 20) {
			Get();
			printer.print("Type: " + t.val);
			printer.print("Next: " + la.val);
			type = INTEGER;
			printer.endProduction("Type");
			
		} else if (la.kind == 21) {
			Get();
			printer.print("Type: " + t.val);
			type = BOOLEAN;
			printer.endProduction("Type");
			
		} else SynErr(30);
		return type;
	}

	void Statement() {
		int type; SymbolTable.Symbol symbol;
		printer.startProduction("Statement"); 
		if (la.kind == 8) {
			Get();
			Expect(9);
			type = Expr();
			Expect(10);
			if (type != BOOLEAN) {
			// ERROR, non-boolean expression in conditional
			this.SemErr("incorrect if-declaration: expected boolean expression");
			}
			else {
			printer.print("if ok");
			}
			int ifEndLabel = this.generator.newLabel();
			this.generator.emit("JZE", ifEndLabel);
			
			Expect(11);
			printer.print("then");
			
			Statement();
			printer.print("fi");
			this.generator.emit("LAB", ifEndLabel);
			printer.endProduction("Statement");
			
		} else if (la.kind == 12) {
			Get();
			printer.print("while");
			int whileEndLabel = this.generator.newLabel();
			
			Expect(9);
			type = Expr();
			Expect(10);
			if (type != BOOLEAN) {
			// ERROR, non-boolean expression
			this.SemErr("incorrect while-declaration: expected boolean expression");
			}
			else {
			printer.print("while ok");
			}
			this.generator.emit("JZE", whileEndLabel);
			
			Statement();
			printer.endProduction("Statement");
			this.generator.emit("LAB", whileEndLabel);
			
		} else if (la.kind == 13) {
			Get();
			printer.print("print");
			Expect(9);
			type = Expr();
			Expect(10);
			this.generator.emit("WRI");
			
			Expect(6);
			printer.endProduction("Statement");
			
		} else if (la.kind == 4) {
			Get();
			StatementList();
			Expect(5);
			printer.endProduction("Statement");
			
		} else if (la.kind == 1) {
			symbol = IdAccess();
			Expect(14);
			type = Expr();
			if (symbol.type == type) {
			this.generator.emit("STL");
			}
			              else {
			                  SemErr("Type mismatch in assignment: " + symbol.type + " </- " + type);
			              }
			
			Expect(6);
			printer.endProduction("Statement");
			
		} else SynErr(31);
	}

	int  Expr() {
		int  type;
		type = UNDEFINED;
		int type1 = UNDEFINED;
		int type2 = UNDEFINED;
		OpData op;
		printer.startProduction("Expr");
		
		if (StartOf(3)) {
			type1 = BaseExpr();
			type = type1; 
			if (StartOf(4)) {
				op = op();
				if (type1 != op.expected) {
				SemErr("Unexpected type for " + op.cw);
				}
				type = op.result;
				
				type2 = BaseExpr();
				if (type2 != op.expected) {
				SemErr("Unexpected type for " + op.cw);
				}
				if (op.cw == "AND") {
				this.generator.emitAnd();
				}
				else {
				this.generator.emit(op.cw);
				}
				printer.endProduction("Expr");
			} else if (la.kind == 6 || la.kind == 10) {
				printer.endProduction("Expr");
			} else SynErr(32);
		} else if (la.kind == 15) {
			Get();
			printer.print("!"); 
			type = BaseExpr();
			if (type != BOOLEAN) {
			// Error, negating non-boolean
			this.SemErr("Trying to negate non-boolean expression");
			}
			else {
			this.generator.emit("NOT");
			}
			
		} else if (la.kind == 16) {
			Get();
			printer.print("-"); 
			type = BaseExpr();
			if (type != INTEGER) {
			   // Error, minus of non-integer
			   this.SemErr("trying to take negative of non-integer");
			}
			else {
			   this.generator.emit("UMN");
			}
			
		} else SynErr(33);
		return type;
	}

	SymbolTable.Symbol  IdAccess() {
		SymbolTable.Symbol  symbol;
		String name;
		symbol = table.new Symbol("undefined", UNDEFINED);
		
		Expect(1);
		name = t.val;
		try {
			symbol = (SymbolTable.Symbol)table.get(name);
			this.generator.emit("ENT", symbol.address);
		} catch (SymbolTable.SymbolNotFoundException e) {
		SemErr("Undeclared variable " + name);
		}
		
		return symbol;
	}

	int  BaseExpr() {
		int  type;
		SymbolTable.Symbol symbol;
		type = UNDEFINED;
		printer.startProduction("BaseExpr");
		switch (la.kind) {
		case 9: {
			Get();
			
			type = Expr();
			Expect(10);
			printer.endProduction("BaseExpr"); 
			break;
		}
		case 1: {
			symbol = IdAccess();
			this.generator.emit("LDL");
			type = symbol.type;
			printer.endProduction("BaseExpr");
			
			break;
		}
		case 2: {
			Get();
			printer.print(t.val);
			int value = this.checkInteger(t.val);
			type = INTEGER;
			this.generator.emit("ENT", value);
			printer.endProduction("BaseExpr");
			
			break;
		}
		case 17: {
			Get();
			printer.print(t.val);
			this.generator.emit("ENT", 1);
			type = BOOLEAN;
			printer.endProduction("BaseExpr");
			
			break;
		}
		case 18: {
			Get();
			printer.print(t.val);
			this.generator.emit("ENT", 0);
			type = BOOLEAN;
			printer.endProduction("BaseExpr");
			
			break;
		}
		case 19: {
			Get();
			type = INTEGER;
			this.generator.emit("REA");
			
			break;
		}
		default: SynErr(34); break;
		}
		return type;
	}

	OpData  op() {
		OpData  op;
		printer.startProduction("op");
		printer.print(la.val);
		op = new OpData();
		op.cw = "";
		op.expected = UNDEFINED;
		op.result = UNDEFINED;
		
		switch (la.kind) {
		case 22: {
			Get();
			op.cw = "ADD";
			op.expected = INTEGER;
			op.result = INTEGER;
			
			break;
		}
		case 16: {
			Get();
			op.cw = "SUB";
			op.expected = INTEGER;
			op.result = INTEGER;
			
			break;
		}
		case 23: {
			Get();
			op.cw = "MUL";
			op.expected = INTEGER;
			op.result = INTEGER;
			
			break;
		}
		case 24: {
			Get();
			op.cw = "DIV";
			op.expected = INTEGER;
			op.result = INTEGER;
			
			break;
		}
		case 25: {
			Get();
			op.cw = "RLT";
			op.expected = INTEGER;
			op.result = BOOLEAN;
			
			break;
		}
		case 26: {
			Get();
			op.cw = "AND";
			op.expected = BOOLEAN;
			op.result = BOOLEAN;
			
			break;
		}
		default: SynErr(35); break;
		}
		return op;
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Grammar();
		Expect(0);

	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_T,_x,_x, _T,_x,_x,_T, _T,_x,_x,_x, _T,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_T,_x,_x, _T,_x,_x,_x, _T,_x,_x,_x, _T,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_T,_T,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x,_x, _x,_T,_T,_T, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _T,_x,_x,_x, _x,_x,_T,_T, _T,_T,_T,_x, _x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "identifier expected"; break;
			case 2: s = "integer expected"; break;
			case 3: s = "\"main\" expected"; break;
			case 4: s = "\"begin\" expected"; break;
			case 5: s = "\"end\" expected"; break;
			case 6: s = "\";\" expected"; break;
			case 7: s = "\"return\" expected"; break;
			case 8: s = "\"if\" expected"; break;
			case 9: s = "\"(\" expected"; break;
			case 10: s = "\")\" expected"; break;
			case 11: s = "\"then\" expected"; break;
			case 12: s = "\"while\" expected"; break;
			case 13: s = "\"print\" expected"; break;
			case 14: s = "\"<-\" expected"; break;
			case 15: s = "\"!\" expected"; break;
			case 16: s = "\"-\" expected"; break;
			case 17: s = "\"true\" expected"; break;
			case 18: s = "\"false\" expected"; break;
			case 19: s = "\"read()\" expected"; break;
			case 20: s = "\"int\" expected"; break;
			case 21: s = "\"boolean\" expected"; break;
			case 22: s = "\"+\" expected"; break;
			case 23: s = "\"*\" expected"; break;
			case 24: s = "\"/\" expected"; break;
			case 25: s = "\"<\" expected"; break;
			case 26: s = "\"&&\" expected"; break;
			case 27: s = "??? expected"; break;
			case 28: s = "invalid VarDecl"; break;
			case 29: s = "invalid StatementList"; break;
			case 30: s = "invalid Type"; break;
			case 31: s = "invalid Statement"; break;
			case 32: s = "invalid Expr"; break;
			case 33: s = "invalid Expr"; break;
			case 34: s = "invalid BaseExpr"; break;
			case 35: s = "invalid op"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
