import java.io.File;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		Compiler compiler = new Compiler();
		File file = new File("tests/"); // Directory containing test inputs

		// Run tests on all test cases
		String name;
		for ( File current : file.listFiles() ) {
			name = current.getPath();
			System.out.println(name);
			compiler.compile(name);

			System.out.println("\n######################\n");
		}
		
		compiler.compile("tests/assignment.tst");
		System.out.println("\n######################\n");
		compiler.compile("tests/and.tst");
	}

}
