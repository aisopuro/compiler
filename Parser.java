

import java.util.Deque;
import java.util.ArrayDeque;
import java.lang.Integer;



public class Parser {
	public static final int _EOF = 0;
	public static final int _identifier = 1;
	public static final int _integer = 2;
	public static final int maxT = 28;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	private Printer printer;

private class Context {
	public int currentType;
	public int expected;
	public CodeGenerator.ops previousOp;

	public Context(int current, int next, CodeGenerator.ops previousOp) {
		this.currentType = current;
		this.expected = next;
		this.previousOp = previousOp;
	}

	public void resetTo(int v, CodeGenerator.ops last) {
		this.currentType = v;
		this.expected = v;
		this.previousOp = last;
	}
}

private SymbolTable table = new SymbolTable();
private CodeGenerator generator = new CodeGenerator();
private Deque<Integer> stack = new ArrayDeque<Integer>();
private Deque<Context> contextStack = new ArrayDeque<Context>();



// Token types
private static final int UNDEFINED = 0;
private static final int INTEGER = UNDEFINED + 1;
private static final int BOOLEAN = INTEGER + 1;

// @SLX

private Context currentContext;

private String id;
private int expected = UNDEFINED; // Type of token expected next
private int lastType = UNDEFINED; // Type of token last processed
private int currentType = UNDEFINED; // Token type to be returned from current indentation
private boolean afterOperator = false; //
private int previousOp;

private String out;

// Reset type context
private void resetContext() {
	this.currentType = UNDEFINED;
	this.expected = UNDEFINED;
	this.lastType = UNDEFINED;
}

private void updateContext(int newCurrent, int newExpected) {
	this.currentType = newCurrent;
	this.expected = newExpected;
	this.currentContext.currentType = newCurrent;
	this.currentContext.expected = newExpected;
}

private boolean isCurrentContextValid(int previousType, int expected) {
	if (!(this.currentType == this.expected || this.currentType == UNDEFINED)) {
		printer.print("Incorrect context");
		this.SemErr("unexpected operation type");
	}
	if (!(this.lastType == previousType)) {
		printer.print("Incompatible type");
		this.SemErr("unexpected type");
	}
	return ((this.currentType == expected || this.currentType == UNDEFINED) && this.lastType == previousType);
}

private boolean isCurrentContextValid(int expected) {
	return isCurrentContextValid(expected, expected);
}

private void enterParenthesis(int currentContextType) {
	this.stack.push(currentContextType);
	this.currentType = this.expected;
	this.expected = UNDEFINED;
	this.lastType = UNDEFINED;
	this.contextStack.push(this.currentContext);
	this.currentContext = new Context(this.currentType, this.expected, CodeGenerator.ops.NOP);
}

private void exitParenthesis() {
	this.lastType = this.currentType;
	this.currentType = this.stack.pop();
	this.expected = UNDEFINED;
	this.afterOperator = false;
	this.currentContext = this.contextStack.pop();
	if (this.currentContext.previousOp != CodeGenerator.ops.NOP) {
		// An operation is scheduled for the result of the parenthesis
		this.generator.commandOp(this.currentContext.previousOp);
	}
}

// Store new variable onto the heap
private boolean storeNewVariable(String name, int type) {
	boolean taken = this.table.exists(name);
	if (this.expected == UNDEFINED) {
		// Error, missing Type declaration
		this.SemErr("undeclared variable type");
	}
	else {
		if (taken) {
			// Error, variable already declared
			this.SemErr("a variable by that name already exists");
		}
		else {
			// Ok, add variable
			int address = this.generator.allocate();
			this.table.add(name, type, address);
			// @SLX: DONE
		}
	}
	return !taken;
}

// Check if a type is within expected parameters
private boolean isExpected(int type) {
	boolean unexpected = (this.expected != type && this.expected != UNDEFINED);
	if (unexpected) {
		// Incompatible types
		printer.print("Error: incompatible types, got integer, expected: " + expected);
		this.SemErr("type mismatch");
	}
	return unexpected;
}

private void checkInteger(String value) {
	int val = Integer.MAX_VALUE;
	try {
		val = Integer.valueOf(value);
	}
	catch (NumberFormatException e) {
		// Number is too large
		this.SemErr("overflow: integer is too large");
	}

	this.lastType = INTEGER;
	this.isExpected(INTEGER);
	this.generator.push(val);
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
		this.currentContext = new Context(0, 0, CodeGenerator.ops.NOP);
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
		printer.startProduction("VarDecl"); 
		if (la.kind == 21 || la.kind == 22) {
			Type();
			Expect(1);
			printer.print("id: " + t.val);
			this.storeNewVariable(t.val, this.currentContext.expected);
			
			Expect(6);
			this.resetContext();
			
			VarDecl();
			printer.endProduction("VarDecl"); 
		} else if (StartOf(1)) {
			printer.print("_");
			printer.endProduction("VarDecl");
			
		} else SynErr(29);
	}

	void StatementList() {
		printer.startProduction("StatementList"); 
		if (StartOf(2)) {
			Statement();
			StatementList();
			printer.endProduction("StatementList"); 
		} else if (la.kind == 5 || la.kind == 7) {
			printer.endProduction("StatementList"); 
		} else SynErr(30);
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
			this.expected = INTEGER;
			this.currentContext.expected = INTEGER;
			printer.endProduction("Type");
			
		} else if (la.kind == 22) {
			Get();
			printer.print("Type: " + t.val);
			this.expected = BOOLEAN;
			this.currentContext.expected = BOOLEAN;
			printer.endProduction("Type");
			
		} else SynErr(31);
	}

	void Statement() {
		printer.startProduction("Statement"); 
		if (la.kind == 8) {
			Get();
			this.generator.startIf();
			
			Expect(9);
			this.enterParenthesis(BOOLEAN);
			
			Expr();
			Expect(10);
			if (this.currentType != BOOLEAN) {
			// ERROR, non-boolean expression in conditional
			printer.print("Error in if-statement: " + this.currentType);
			this.SemErr("incorrect if-declaration");
			}
			else {
			printer.print("if ok");
			}
			this.exitParenthesis();
			this.resetContext();
			
			Expect(11);
			this.generator.startThen();
			printer.print("then");
			
			Statement();
			Expect(12);
			this.generator.startFi();
			printer.print("fi");
			expected = UNDEFINED;
			printer.endProduction("Statement");
			
		} else if (la.kind == 13) {
			Get();
			printer.print("while");
			stack.push(BOOLEAN);
			this.generator.startWhile();
			
			Expect(9);
			this.enterParenthesis(BOOLEAN);
			Expr();
			Expect(10);
			if (stack.peek() != BOOLEAN) {
			// ERROR, non-boolean expression
			this.SemErr("incorrect while-declaration");
			}
			else {
				printer.print("while ok");
			}
			this.generator.endWhileConditional();
			this.exitParenthesis();
			this.resetContext();
			
			Statement();
			printer.endProduction("Statement");
			this.generator.endWhileBody();
			
		} else if (la.kind == 14) {
			Get();
			printer.print("print");
			Expect(9);
			Expr();
			Expect(10);
			this.generator.commandPrint();
			
			Expect(6);
			printer.endProduction("Statement");
			
		} else if (la.kind == 4) {
			Get();
			StatementList();
			Expect(5);
			printer.endProduction("Statement");
			
		} else if (la.kind == 1) {
			IdAccess();
			Expect(15);
			this.currentContext.previousOp = CodeGenerator.ops.ASG;
			this.enterParenthesis(this.currentType);
			
			Expr();
			if (this.lastType == this.expected) {
			this.generator.commandAssignment();
			}
			
			Expect(6);
			this.exitParenthesis();
			this.resetContext();
			printer.endProduction("Statement");
			
		} else SynErr(32);
	}

	void Expr() {
		printer.startProduction("Expr");
		
		if (StartOf(3)) {
			BaseExpr();
			if (StartOf(4)) {
				op();
				BaseExpr();
				this.generator.commandOp(this.currentContext.previousOp);
				printer.endProduction("Expr");
			} else if (la.kind == 6 || la.kind == 10) {
				printer.endProduction("Expr");
			} else SynErr(33);
		} else if (la.kind == 16) {
			Get();
			printer.print("!");
			this.expected = BOOLEAN;
			// @SLX: Negation here
			
			BaseExpr();
			if (this.lastType != BOOLEAN) {
			// Error, negating non-boolean
			this.SemErr("trying to negate non-boolean");
			}
			else {
			this.generator.commandNegation();
			}
			
		} else if (la.kind == 17) {
			Get();
			printer.print("-");
			this.expected = INTEGER;
			// @SLX: Minus here
			
			BaseExpr();
			if (this.lastType != INTEGER) {
			   // Error, minus of non-integer
			   this.SemErr("trying to take negative of non-integer");
			}
			else {
			   this.generator.commandMinus();
			}
			
		} else SynErr(34);
	}

	void IdAccess() {
		printer.startProduction("IdAccess"); 
		Expect(1);
		if (this.table.exists(t.val)) {
		// id is in table, fetch it to the stack
		this.generator.commandLoad(this.table.addressOf(t.val));
		printer.print("id found: " + t.val);
		if (this.la.val.equals(":=")) {
			// Assignment coming up
			this.currentType = this.table.typeOf(t.val);
		}
		this.lastType = this.table.typeOf(t.val);
		}
		else {
		printer.print("id not found: " + t.val);
		this.SemErr("no variable named " + t.val);
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
			this.checkInteger(t.val);
			printer.endProduction("BaseExpr");
			
			break;
		}
		case 18: {
			Get();
			printer.print(t.val);
			this.lastType = BOOLEAN;
			this.isExpected(BOOLEAN);
			this.generator.push(1);
			printer.endProduction("BaseExpr");
			
			break;
		}
		case 19: {
			Get();
			printer.print(t.val);
			this.lastType = BOOLEAN;
			this.isExpected(BOOLEAN);
			this.generator.push(0);
			printer.endProduction("BaseExpr");
			
			break;
		}
		case 20: {
			Get();
			this.lastType = INTEGER;
			this.isExpected(INTEGER);
			this.generator.commandRead();
			
			break;
		}
		default: SynErr(35); break;
		}
	}

	void op() {
		printer.startProduction("op");
		printer.print(la.val); 
		switch (la.kind) {
		case 23: {
			Get();
			if (!isCurrentContextValid(INTEGER)) {
			}
			this.updateContext(INTEGER, INTEGER);
			this.currentContext.previousOp = CodeGenerator.ops.ADD;
			printer.endProduction("op");
			
			break;
		}
		case 17: {
			Get();
			if (!isCurrentContextValid(INTEGER)) {
			// @SLX: SUB
			}
			this.updateContext(INTEGER, INTEGER);
			this.currentContext.previousOp = CodeGenerator.ops.SUB;
			printer.endProduction("op");
			
			break;
		}
		case 24: {
			Get();
			if (!isCurrentContextValid(INTEGER)) {
			// @SLX: MUL
			}
			this.updateContext(INTEGER, INTEGER);
			this.currentContext.previousOp = CodeGenerator.ops.MUL;
			printer.endProduction("op");
			
			break;
		}
		case 25: {
			Get();
			if (!isCurrentContextValid(INTEGER)) {
			// @SLX: DIV
			}
			this.updateContext(INTEGER, INTEGER);
			this.currentContext.previousOp = CodeGenerator.ops.DIV;
			printer.endProduction("op");
			
			break;
		}
		case 26: {
			Get();
			if (!isCurrentContextValid(INTEGER, BOOLEAN)) {
			// @SLX: LESS_THAN
			}
			this.updateContext(BOOLEAN, INTEGER);
			this.currentContext.previousOp = CodeGenerator.ops.LSS;
			printer.endProduction("op");
			
			break;
		}
		case 27: {
			Get();
			if (!isCurrentContextValid(BOOLEAN)) {
			// @SLX: AND
			}
			this.updateContext(BOOLEAN, BOOLEAN);
			this.currentContext.previousOp = CodeGenerator.ops.AND;
			printer.endProduction("op");
			
			break;
		}
		default: SynErr(36); break;
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
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x},
		{_x,_T,_x,_x, _T,_x,_x,_T, _T,_x,_x,_x, _x,_T,_T,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x},
		{_x,_T,_x,_x, _T,_x,_x,_x, _T,_x,_x,_x, _x,_T,_T,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x},
		{_x,_T,_T,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_T,_T, _T,_x,_x,_x, _x,_x,_x,_x, _x,_x},
		{_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x,_T, _T,_T,_T,_T, _x,_x}

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
			case 12: s = "\"fi\" expected"; break;
			case 13: s = "\"while\" expected"; break;
			case 14: s = "\"print\" expected"; break;
			case 15: s = "\"<-\" expected"; break;
			case 16: s = "\"!\" expected"; break;
			case 17: s = "\"-\" expected"; break;
			case 18: s = "\"true\" expected"; break;
			case 19: s = "\"false\" expected"; break;
			case 20: s = "\"read()\" expected"; break;
			case 21: s = "\"int\" expected"; break;
			case 22: s = "\"boolean\" expected"; break;
			case 23: s = "\"+\" expected"; break;
			case 24: s = "\"*\" expected"; break;
			case 25: s = "\"/\" expected"; break;
			case 26: s = "\"<\" expected"; break;
			case 27: s = "\"&&\" expected"; break;
			case 28: s = "??? expected"; break;
			case 29: s = "invalid VarDecl"; break;
			case 30: s = "invalid StatementList"; break;
			case 31: s = "invalid Type"; break;
			case 32: s = "invalid Statement"; break;
			case 33: s = "invalid Expr"; break;
			case 34: s = "invalid Expr"; break;
			case 35: s = "invalid BaseExpr"; break;
			case 36: s = "invalid op"; break;
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
