

import java.util.Deque;
import java.util.ArrayDeque;
import java.lang.Integer;

public class Parser {
	public static final int _EOF = 0;
	public static final int _identifier = 1;
	public static final int _integer = 2;
	public static final int maxT = 29;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	private Printer printer;

private SymbolTable table = new SymbolTable();
private CodeGenerator generator = new CodeGenerator();
private Deque<Integer> stack = new ArrayDeque<Integer>();

private static final int UNDEFINED = 0;
private static final int INTEGER = UNDEFINED + 1;
private static final int BOOLEAN = INTEGER + 1;


private String id;
private int expected = UNDEFINED;
private int lastType = UNDEFINED;
private int currentType = UNDEFINED;
private int address = 0;

private String out;

private void resetContext() {
	this.currentType = UNDEFINED;
	this.expected = UNDEFINED;
	this.lastType = UNDEFINED;
}

private void updateContext(int newCurrent, int newExpected) {
	this.currentType = newCurrent;
	this.expected = newExpected;
}

private boolean isCurrentContextValid(int previousType, int expected) {
	if (!(this.currentType == expected || this.currentType == UNDEFINED))
		printer.print("Incorrect context");
	if (!(this.lastType == previousType))
		printer.print("Incompatible type");
	return ((this.currentType == expected || this.currentType == UNDEFINED) && this.lastType == previousType);
}

private boolean isCurrentContextValid(int expected) {
	return isCurrentContextValid(expected, expected);
}

private void enterParenthesis(int currentContextType) {
	this.stack.push(currentContextType);
	this.currentType = this.expected;
	this.expected = UNDEFINED;
}

private void exitParenthesis() {
	this.lastType = this.currentType;
	this.currentType = this.stack.pop();
	this.expected = UNDEFINED;
}

public Parser(Scanner s, Printer p) {
       this(s);
       this.printer = p;
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
		stack.push(UNDEFINED); // Set dummy value for stack bottom
		printer.startProduction("Grammar"); 
		
		MainFuncDecl();
		Expect(0);
		printer.print("Table: " + table.toString());
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
		printer.startProduction("VarDecl"); 
		if (la.kind == 21 || la.kind == 22) {
			Type();
			Expect(1);
			printer.print("id: " + t.val);
			if (expected != UNDEFINED) {
				if (!table.exists(t.val)) {
					// New variable
					table.add(t.val, stack.peek(), 0);
					generator.allocate(lastType);
					generator.printProgram();
					lastType = UNDEFINED;
					printer.print("New variable: " + t.val + ":" + table.typeOf(t.val));
					printer.print("Next: " + la.val);
					
				} 
				else {
					// Variable already declared
				}
			}
			else {
				// Invalid or missing type
			}
			expected = UNDEFINED;
			
			Expect(6);
			VarDecl();
			printer.endProduction("VarDecl"); 
		} else if (StartOf(1)) {
			printer.print("_");
			printer.endProduction("VarDecl"); 
			
		} else SynErr(30);
	}

	void StatementList() {
		printer.startProduction("StatementList"); 
		if (StartOf(2)) {
			Statement();
			StatementList();
			printer.endProduction("StatementList"); 
		} else if (la.kind == 5 || la.kind == 7) {
			printer.endProduction("StatementList"); 
		} else SynErr(31);
	}

	void ReturnStatement() {
		printer.startProduction("ReturnStatement"); 
		Expect(7);
		printer.print("return"); 
		Expr();
		Expect(6);
		printer.endProduction("ReturnStatement"); 
	}

	void Type() {
		printer.startProduction("Type"); 
		
		if (la.kind == 21) {
			Get();
			printer.print("Type: " + t.val);
			printer.print("Next: " + la.val);
			expected = INTEGER;
			printer.endProduction("Type");
			
		} else if (la.kind == 22) {
			Get();
			printer.print("Type: " + t.val);
			expected = BOOLEAN;
			printer.endProduction("Type");
			
		} else SynErr(32);
	}

	void Statement() {
		printer.startProduction("Statement"); 
		if (la.kind == 8) {
			Get();
			
			Expect(9);
			this.enterParenthesis(BOOLEAN);
			
			Expr();
			Expect(10);
			if (this.currentType != BOOLEAN) {
			// ERROR, non-boolean expression in conditional
			printer.print("Error in if-statement: " + this.currentType);
			}
			else {
				printer.print("if ok");
			}
			stack.pop();
			this.resetContext();
			
			Expect(11);
			printer.print("then");
			Statement();
			Expect(12);
			printer.print("else");
			Statement();
			Expect(13);
			printer.print("fi");
			expected = UNDEFINED;
			printer.endProduction("Statement"); 
			
		} else if (la.kind == 14) {
			Get();
			printer.print("while");
			stack.push(BOOLEAN);
			
			Expect(9);
			Expr();
			Expect(10);
			if (stack.peek() != BOOLEAN) {
			// ERROR, non-boolean expression
			printer.print("Error in while-statement");
			}
			else {
				printer.print("while ok");
			}
			expected = UNDEFINED;
			stack.pop();
			
			Statement();
			printer.endProduction("Statement");
		} else if (la.kind == 15) {
			Get();
			printer.print("print");
			Expect(9);
			Expr();
			Expect(10);
			Expect(6);
			printer.endProduction("Statement"); 
			
		} else if (la.kind == 4) {
			Get();
			StatementList();
			Expect(5);
			printer.endProduction("Statement"); 
			
		} else if (la.kind == 1) {
			IdAccess();
			Expect(16);
			Expr();
			Expect(6);
			expected = UNDEFINED;
			printer.endProduction("Statement"); 
			
		} else SynErr(33);
	}

	void Expr() {
		printer.startProduction("Expr"); 
		printer.print(t.val);
		if (StartOf(3)) {
			BaseExpr();
			if (StartOf(4)) {
				op();
				BaseExpr();
				printer.endProduction("Expr");
			} else if (la.kind == 6 || la.kind == 10) {
				printer.endProduction("Expr");
			} else SynErr(34);
		} else if (la.kind == 17) {
			Get();
			printer.print("!"); 
			BaseExpr();
		} else SynErr(35);
	}

	void IdAccess() {
		printer.startProduction("IdAccess"); 
		Expect(1);
		if (table.exists(t.val)) {
		// id is in table
		printer.print("id found: " + t.val);
		}
		else {
			printer.print("id not found: " + t.val);
		}
		
		if (la.val.equals(":=")) {
			currentType = table.typeOf(t.val);
		}
		printer.endProduction("IdAccess"); 
	}

	void BaseExpr() {
		printer.startProduction("BaseExpr");
		switch (la.kind) {
		case 9: {
			Get();
			printer.print(stack.size() + ": In with: " + currentType);
			this.enterParenthesis(this.currentType);
			
			Expr();
			Expect(10);
			this.exitParenthesis();
			printer.print(stack.size() + ": Out with: " + lastType);
			printer.endProduction("BaseExpr"); 
			break;
		}
		case 1: {
			IdAccess();
			printer.endProduction("BaseExpr");
			break;
		}
		case 2: {
			Get();
			printer.print(t.val);
			lastType = INTEGER;
			if (expected == INTEGER || expected == UNDEFINED) {
				// Ok
			}
			else {
				// Incompatible types
				printer.print("Error: incompatible types, got integer, expected: " + expected);
			}
			printer.endProduction("BaseExpr");
			
			break;
		}
		case 18: {
			Get();
			printer.print(t.val);
			lastType = BOOLEAN;
			if (expected == BOOLEAN || expected == UNDEFINED) {
				// Ok
			}
			else {
				// Incompatible types
				printer.print("Error: incompatible types, got boolean, expected: " + expected);
			}
			printer.endProduction("BaseExpr"); 
			
			break;
		}
		case 19: {
			Get();
			printer.print(t.val);
			if (expected == BOOLEAN || expected == UNDEFINED) {
				// Ok
			}
			else {
				// Incompatible types
				printer.print("Error: incompatible types, got boolean, expected: " + expected);
			}
			printer.endProduction("BaseExpr"); 
			
			break;
		}
		case 20: {
			Get();
			if (!(expected == expected)) {
			// Incompatible types
			printer.print("Error: incompatible types, got integer, expected: " + expected);
			}
			else {
				// Correct
			}
			printer.endProduction("BaseExpr"); 
			
			break;
		}
		default: SynErr(36); break;
		}
	}

	void op() {
		printer.startProduction("op");
		printer.print(la.val); 
		switch (la.kind) {
		case 23: {
			Get();
			if (!isCurrentContextValid(INTEGER)) {
			// ERROR, incompatible types
			printer.print("Cannot add to boolean");
			}
			this.updateContext(INTEGER, INTEGER);
			printer.endProduction("op"); 
			
			break;
		}
		case 24: {
			Get();
			if (!isCurrentContextValid(INTEGER)) {
			// ERROR, incompatible types
			printer.print("Cannot subtract from boolean");
			}
			this.updateContext(INTEGER, INTEGER);
			printer.endProduction("op"); 
			
			break;
		}
		case 25: {
			Get();
			if (!isCurrentContextValid(INTEGER)) {
			// ERROR, incompatible types
			printer.print("Cannot multiply boolean");
			}
			this.updateContext(INTEGER, INTEGER);
			printer.endProduction("op"); 
			
			break;
		}
		case 26: {
			Get();
			if (!isCurrentContextValid(INTEGER)) {
			// ERROR, incompatible types
			printer.print("Cannot divide boolean");
			}
			this.updateContext(INTEGER, INTEGER);
			printer.endProduction("op"); 
			
			break;
		}
		case 27: {
			Get();
			if (!isCurrentContextValid(INTEGER, BOOLEAN)) {
			// ERROR, incompatible types
			printer.print("Cannot use relational operator on boolean");
			printer.print("" + this.lastType + " " + this.currentType);
			}
			this.updateContext(BOOLEAN, INTEGER);
			printer.endProduction("op"); 
			
			break;
		}
		case 28: {
			Get();
			if (!isCurrentContextValid(BOOLEAN)) {
			// ERROR, incompatible types
			printer.print("Cannot use AND-operator on integers");
			}
			this.updateContext(BOOLEAN, BOOLEAN);
			printer.endProduction("op"); 
			
			break;
		}
		default: SynErr(37); break;
		}
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Grammar();

		Expect(0);
	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,x,x, T,x,x,T, T,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,x,x, T,x,x,x, T,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,T,x, x,x,x,x, x,T,x,x, x,x,x,x, x,x,T,T, T,x,x,x, x,x,x,x, x,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,x,x}

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
			case 4: s = "\"{\" expected"; break;
			case 5: s = "\"}\" expected"; break;
			case 6: s = "\";\" expected"; break;
			case 7: s = "\"return\" expected"; break;
			case 8: s = "\"if\" expected"; break;
			case 9: s = "\"(\" expected"; break;
			case 10: s = "\")\" expected"; break;
			case 11: s = "\"then\" expected"; break;
			case 12: s = "\"else\" expected"; break;
			case 13: s = "\"fi\" expected"; break;
			case 14: s = "\"while\" expected"; break;
			case 15: s = "\"print\" expected"; break;
			case 16: s = "\":=\" expected"; break;
			case 17: s = "\"!\" expected"; break;
			case 18: s = "\"true\" expected"; break;
			case 19: s = "\"false\" expected"; break;
			case 20: s = "\"read()\" expected"; break;
			case 21: s = "\"int\" expected"; break;
			case 22: s = "\"boolean\" expected"; break;
			case 23: s = "\"+\" expected"; break;
			case 24: s = "\"-\" expected"; break;
			case 25: s = "\"*\" expected"; break;
			case 26: s = "\"/\" expected"; break;
			case 27: s = "\"<\" expected"; break;
			case 28: s = "\"&&\" expected"; break;
			case 29: s = "??? expected"; break;
			case 30: s = "invalid VarDecl"; break;
			case 31: s = "invalid StatementList"; break;
			case 32: s = "invalid Type"; break;
			case 33: s = "invalid Statement"; break;
			case 34: s = "invalid Expr"; break;
			case 35: s = "invalid Expr"; break;
			case 36: s = "invalid BaseExpr"; break;
			case 37: s = "invalid op"; break;
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

