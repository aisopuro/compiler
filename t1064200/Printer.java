/**
 * A class for printing a parse tree.
 */
public class Printer {
    /** The current indentation depth */
    private int depth = 0;
    private String indent = new String();
    /** If <code>true</code> the printer won't print anything. */
    private boolean silent = false;

    /**
     * Construct a new Printer.
     *
     * @param silent If <code>true</code> the printer won't print
     * anything.
     */
    public Printer(boolean silent) {
        this.silent = silent;
    }

    /**
     * Increase the indentation depth.
     */
    private void incDepth() {
        this.depth++;
        indent();
    }

    /**
     * Decrease the indentation depth.
     */
    private void decDepth() {
        this.depth--;
        indent();
    }

    /**
     * Build appropriate indentation string
     */
    private void indent() {
        if (!silent) {
        	this.indent = new String();
            for(int i = 0; i < depth; i++) {
                this.indent += "    ";
            }
        }
    }

    /**
     * Print a string on its own line.
     * @param s the string to be printed
     */
    private void println(String s) {
        if (!silent)
            System.out.println(this.indent + s);
    }

    /**
     * Print a string and indent.
     * @param s the string to be printed.
     */
    public void print(String s) {
        println(s);
    }

    /**
     * Open the parse tree node by printing "(<name>\n" and increasing
     * indentation.
     *
     * @param name the name of the nonterminal.
     */
    public void startProduction(String name) {
        incDepth();
        println("(" + name);
    }

    /**
     * Close the parse tree node by printing ")" and decreasing
     * indentation.
     */
    public void endProduction() {
    	this.endProduction("");
    }
    
    public void endProduction(String name) {
        println(")" + name);
        decDepth();
    }
}
