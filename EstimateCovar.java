import java.util.ArrayList;
import java.io.*;
import java.util.Collections;

/** Script to parse MS output trees and extract Coalescence times,
 *  sort them, and then compute E(T_iT_j) covariance estimates.
 *  Tree-building code inspired by Eddie Ma's JavaScript implementation:
 *  http://eddiema.ca/2010/06/25/parsing-a-newick-tree-with-recursive-descent/ 
 *  
 *  @author Jonas Mueller
 */

public class EstimateCovar {

    /** Command: "java estimateCovar <File>" will return the E(T_iT_j)
     *  estimate for the intercoalescence times in that file. */
    public static void main(String[] args) throws Exception {
	FileReader fr = new FileReader(args[0]);
	BufferedReader br = new BufferedReader(fr);
	String line = "";
	ArrayList<Double> ETiTjValues = new ArrayList<Double>();
	// Skip header of file //
       	while (!line.equals("//")) {
	    line = br.readLine();
	}
	while (line != null) {
	    line = br.readLine();
	    if (!((line == null) || line.equals("//") || line.equals("")))  {
		// Only consider lines representing Newick trees //
		NewickTree tree = new NewickTree();
		TreeNode root = tree.build(line);
		// Compute E(TiTj) from the above tree and add it to list //
		ETiTjValues.add(new Double(computeETiTj(root)));
	    }
	}
      	double estimatedETiTj = average(ETiTjValues);
	System.out.println("Estimated E(T_iT_j) = " + estimatedETiTj);
    }

    /** Returns E(TiTj) calculated from a TreeNode representing the
     *  ROOT of a coalescence tree. */
    public static double computeETiTj(TreeNode root) {
	ArrayList<Double> coalescence_times = new ArrayList<Double>();
	getTimes(root, coalescence_times); // fill list with times from tree.
	Collections.sort(coalescence_times);
	// Create list of intercoalescence times (units: 2*N_0 generations) //
	ArrayList<Double> intercoalescence_times = new ArrayList<Double>();
	double first_time = coalescence_times.get(0).doubleValue() * 2;
	intercoalescence_times.add(new Double(first_time));
	for (int i = 1; i < coalescence_times.size(); i++) {
	    double last_time = coalescence_times.get(i - 1).doubleValue() * 2;
	    double next_time = coalescence_times.get(i).doubleValue() * 2;
	    double interval = next_time - last_time;
	    if (interval < 0) {
		// error checking... //
		System.err.println("Error in times");
		System.exit(1);
	    }
	    intercoalescence_times.add(new Double(interval));
	}
	// Use this list to compute E(TiTj) and return it //
	return expectedProduct(intercoalescence_times);
    }

    /** Returns the expecated value of the product of a pair X_i, X_j
     *  (with i <= j) in the list DOUBLES. */
    public static double expectedProduct(ArrayList<Double> doubles) {
	double sum = 0; // records the sum of the pairwise products.
	double count = 0; // counts the number of pairs.
	for (int i = 0; i < doubles.size(); i++) {
	    for (int j = i; j < doubles.size(); j++) {
		double ith = doubles.get(i).doubleValue();
		double jth = doubles.get(j).doubleValue();
		sum += (ith * jth);
		count++;
	    }
	}
	return (sum / count);
    }

    /** Lists the TIMES of coalescence in the tree with the given ROOT
     *  in an Arraylist, by performing a postorder traversal. Relies on
     *  the fact that the tree has symmetric binary structure. */
    public static void getTimes(TreeNode root, ArrayList<Double> times) {
	if (root.left == null) {
	    // Node is a leaf and thus has coalescence time 0 //
	    root.coalescence_time = 0;
	} else {
	    getTimes(root.left, times); // recurse on left subtree.
	    getTimes(root.right, times); // recurse on right subtree.
	    // Use branch length of left child to compute coalescence time //
	    root.coalescence_time =
		root.left.coalescence_time + root.left.branch_length;
	    times.add(new Double(root.coalescence_time));
	}
    }

    /** Returns the average of ArrayList DOUBLES. */
    public static double average(ArrayList<Double> doubles) {
	double sum = 0;
	for (int i = 0; i < doubles.size(); i++) {
	    sum += doubles.get(i).doubleValue();
	}
	return (sum / doubles.size());
    }
}
