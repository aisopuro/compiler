/**
 * 
 */
import java.util.ArrayDeque;
import java.util.Deque;

import fi.tkk.cs.tkkcc.slx.SlxProgram;
import fi.tkk.cs.tkkcc.slx.CommandWord;

/**
 * @author aisopuro
 * 
 */
public class CodeGenerator {
	private SlxProgram assembler;
	
	public enum ops {
		NOP, ADD, SUB, MUL, DIV, LSS, AND, ASG
	}
	
	private class flowLabels {
		public int controlLabel;
		public int endLabel;
		
		public flowLabels(int control, int end) {
			this.controlLabel = control;
			this.endLabel = end;
		}
	}
	
	private Deque<flowLabels> currentLabels = new ArrayDeque<flowLabels>(); // Stack for keeping track of if-else nesting
	
	private int offset = 0;

	public CodeGenerator() {
		this.assembler = new SlxProgram();
	}

	public int allocate() {
		this.assembler.emit(CommandWord.ENT, 1); // No arrays, only singe numbers
		this.assembler.emit(CommandWord.ALC);
		return this.newAddress();
	}
	private int newAddress() {
		int a = this.offset;
		this.offset++;
		return a;
	}
	
	public void push(int value) {
		this.assembler.emit(CommandWord.ENT, value);
	}
	
	public int getLatestLineNumber() {
		return this.assembler.getProgram().size();
	}
	
	public void commandPrint() {
		this.assembler.emit(CommandWord.WRI);
	}
	
	public void commandNegation() {
		this.assembler.emit(CommandWord.NOT);
	}
	
	public void commandRead() {
		this.assembler.emit(CommandWord.REA);
	}
	
	public void commandOp(CodeGenerator.ops opType) {
		if (opType == ops.ADD) {
			this.assembler.emit(CommandWord.ADD);
		}
		else if (opType == ops.SUB) {
			// Subtract
                    this.assembler.emit(CommandWord.SUB);
		}
		else if (opType == ops.MUL) {
			// Multiply
                    this.assembler.emit(CommandWord.MUL);
		}
		else if (opType == ops.DIV) {
			// Divide
                    this.assembler.emit(CommandWord.DIV);
		}
		else if (opType == ops.LSS) {
			// Less than
                    this.assembler.emit(CommandWord.RLT);
		}
		else if (opType == ops.AND) {
			// And
		    // (pop == true) == pop
                    this.assembler.emit(CommandWord.ENT, 1);
                    this.assembler.emit(CommandWord.REQ);
                    this.assembler.emit(CommandWord.REQ);
		}
		else {
			// NOP
		}
	}
	
	public void commandAssignment() {
		this.assembler.emit(CommandWord.STL);
	}
	
	public void commandLoad(int address) {
		this.assembler.emit(CommandWord.ENT, address);
		this.assembler.emit(CommandWord.LDM);
	}
	
	public void startIf() {
		// Create new label context and push it on the stack
		flowLabels currentIndent = new flowLabels(this.newAddress(), this.newAddress());
		this.currentLabels.push(currentIndent);
	}
	
	public void startThen() {
		// If pop is false, goto elseLabel, else continue with then
		this.assembler.emit(CommandWord.JZE, this.currentLabels.peek().controlLabel);
	}
	
	public void startElse() {
		// If we came through then, jump to fi
		this.assembler.emit(CommandWord.JMP, this.currentLabels.peek().endLabel);
		// Declare elseLabel
		this.assembler.emit(CommandWord.LAB, this.currentLabels.peek().controlLabel);
	}
	
	public void startFi() {
		// Declare fiLabel
		this.assembler.emit(CommandWord.LAB, this.currentLabels.peek().endLabel);
		// Reached the end of an if-else, pop the stack
		this.currentLabels.pop();
	}
	
	public void startWhile() {
		// Create new label context onto the stack
		this.currentLabels.push(new flowLabels(this.newAddress(), this.newAddress()));
		// Label the beginning of the while loop
		this.assembler.emit(CommandWord.LAB, this.currentLabels.peek().controlLabel);
	}
	
	public void endWhileConditional() {
		// If the conditional isn't true, jump to the end
		this.assembler.emit(CommandWord.JZE, this.currentLabels.peek().endLabel);
	}
	
	public void endWhileBody() {
		// if the body was traversed, jump to the beginning
		this.assembler.emit(CommandWord.JMP, this.currentLabels.peek().controlLabel);
		// Declare the label that skips the body
		this.assembler.emit(CommandWord.LAB, this.currentLabels.peek().endLabel);
		// Pop the label context
		this.currentLabels.pop();
	}
	public void halt() {
	    this.assembler.emit(CommandWord.HLT);
	}

	public void printProgram() {
		System.out.println(this.assembler.toString());
	}
}
