import fi.tkk.cs.tkkcc.SlxCompiler;
import fi.tkk.cs.tkkcc.slx.SlxProgram;

public class Compiler implements SlxCompiler {

	public Compiler() {

	}

	private final boolean skipPrint = false; // Set whether to print parsing to
												// console

	@Override
	public boolean isErrors() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SlxProgram compile( String sourceFilename ) {
		Scanner scanner = new Scanner(sourceFilename);
		Parser parser;
		if ( !skipPrint ) {
			Printer printer = new Printer(false);
			parser = new Parser(scanner, printer);
		}
		else {
			parser = new Parser(scanner);
		}
		parser.Parse();
		return null;
	}

}
