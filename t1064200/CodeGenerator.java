/**
 * 
 */
import fi.tkk.cs.tkkcc.slx.SlxProgram;
import fi.tkk.cs.tkkcc.slx.CommandWord;

/**
 * @author aisopuro
 * 
 */
public class CodeGenerator {
	private SlxProgram assembler;

	public CodeGenerator() {
		this.assembler = new SlxProgram();
	}

	public void allocate( int type ) {
		this.assembler.emit(CommandWord.ENT, new Integer(type));
		this.assembler.emit(CommandWord.ALC);
	}

	public void printProgram() {
		System.out.println(this.assembler.toString());
	}
}
