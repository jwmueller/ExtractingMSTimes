/** Class from which the coalescence tree is built. */

public class NewickTree {

    /** Constructs a NewickTree object with counters set to 0. */
    NewickTree() {
	this.count = 0;
	this.cursor = 0;
    }

     /** Returns a TreeNode representing the root of coalescent 
     *	tree described by NEWICK_STRING with
     *  counters COUNT and CURSOR. Assumes whitespace/newlines 
     *  have been removed. */
    public TreeNode build(String newick_string) {
	TreeNode t = new TreeNode();
	// Look for things relating to the left child of this node. //
	if (newick_string.charAt(this.cursor) == '(') {
	    this.cursor++; // move cursor past "(".
	    if (newick_string.charAt(this.cursor) == '(') {
		// left node is internal node: use recursion //
		t.left = build(newick_string); // RECURSIVE CALL.
		t.left.parent = t;
	    }
	    // Try to find a valid name next at the current cursor position //
	     if (newick_string.substring(this.cursor, this.cursor + 1)
		 .matches("[0-9A-Za-z]")) {
		 // Left node is a leaf node: parse the leaf data //
		 t.left = new TreeNode();
		 t.left.parent = t;
		 this.count++; 
		 this.cursor++;
		 // Move cursor past name //
		 while (newick_string.substring(this.cursor, this.cursor + 1)
			.matches("[0-9A-Za-z]")) {
		     this.cursor++;
		 }
	     }
	     // note: if no name is found, just skip to finding branch length.
	}
	if (newick_string.charAt(this.cursor) == ':') {
	    // Expect left branch length after descending into left child //
	    this.cursor++; // move cursor past the colon.
	    // look for a floating point value of the branch length //
	    int floatstart = this.cursor;
	    while (newick_string.substring(this.cursor, this.cursor + 1)
		   .matches("[0-9.eE]")) {
		this.cursor++;
	    }
	    String floatval = newick_string.substring(floatstart, this.cursor);
	    t.left.branch_length = Double.parseDouble(floatval);
	}
	// Look for right node //
	if (newick_string.charAt(this.cursor) == ',') {
	    this.cursor++; // move cursor past comma
	    if (newick_string.charAt(this.cursor) == '(') {
            // Right node is an internal node: use recursion //
            t.right = build(newick_string); // RECURSIVE CALL.
            t.right.parent = t;
        }
        // Try to find a valid name next at the current cursor position //
	    if (newick_string.substring(this.cursor, this.cursor + 1)
		.matches("[0-9A-Za-z]")) {
		// Right node is a leaf node: parse the leaf data //
		t.right = new TreeNode();
		t.right.parent = t;
		this.count++;
		this.cursor++;
		// Move cursor past name //
		while (newick_string.substring(this.cursor, this.cursor + 1)
		       .matches("[0-9A-Za-z]")) {
		    this.cursor++;
		}
	    }
	} // again, if no name found, move on. //
	// Expect right branch length after descending into right child //
	if (newick_string.charAt(this.cursor) == ':') {
	    this.cursor++; // move past colon.
	    // look for a floating point value of the branch length //
	    int start2 = this.cursor;
	    while (newick_string.substring(this.cursor, this.cursor + 1)
		   .matches("[0-9.eE]")) {
		this.cursor++;
	    }
	    String floatval2 = newick_string.substring(start2, this.cursor);
	    t.right.branch_length = Double.parseDouble(floatval2);
	}
	if (newick_string.charAt(this.cursor) == ')') {
        this.cursor++; // move past ")".
	}
	// Expect the branch length of node after reading in both children //
	if (newick_string.charAt(this.cursor) == ':') {
	    this.cursor++;
	    // look for a floating point value of the branch length //
	    int start3 = this.cursor;
	    while (newick_string.substring(this.cursor, this.cursor + 1)
		   .matches("[0-9.eE]")) {
		this.cursor++;
	    }
	    String floatval3 = newick_string.substring(start3, this.cursor);
	    t.branch_length = Double.parseDouble(floatval3);
	}
	if (newick_string.charAt(this.cursor) == ';') {
	    // This case only executes for root node //
	    this.cursor++;
	}
	// Complete a recursive instance (internal) or base case (leaf) //
	return t;
    }

    /** A counter to keep track of the number of nodes in 
     *  the tree being built; stored as array so it can be
     *  passed by reference. */
     public int count;

    /** A counter to determine how much of the string we've parsed
     *  in the process of building a tree; stored as array so it can
     *  be passed by reference. */
    public int cursor;

}