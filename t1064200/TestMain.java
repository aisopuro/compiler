import java.io.File;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		Compiler compiler = new Compiler();
		File file = new File("/home/aisopuro/workspace_java/compiler/t1064200/tests/"); // Directory containing test inputs
		
		// Run tests on all test cases
		String name;
		
		for ( File current : file.listFiles() ) {
			name = current.getPath();
			System.out.println(name);
                        System.out.println("=======================");
			
			compiler.compile(name);

			System.out.println("\n######################\n");
		}
                
		/*
        compiler.compile("/home/aisopuro/workspace_java/compiler/t1064200/tests/assignment.tst");
        System.out.println("\n######################\n");
        //compiler.compile("tests/wrongTypeInAssignmentIntBoolean.tst");
         * */
         
                
	}

}
