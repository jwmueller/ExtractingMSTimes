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

    /** Command: "java estimateCovar <File> > <Outfile>" will return the E(T_iT_j)
     *  estimates for the intercoalescence times in that file, and write the results
     *  as a list, which can be piped to a specified output file.  The first n - 1 entries 
     *  correspond to E(T_2*T_j) for j = 2,...,n, the next n - 1 entries correspond 
     *  to E(T_3*T_j) for j = 2,...,n, etc. */
    public static void main(String[] args) throws Exception {
	FileReader fr = new FileReader(args[0]);
	BufferedReader br = new BufferedReader(fr);
	String line = "";
	double samplecounter = 0; // keeps track of the number of samples.
	ArrayList<Double> ETiTjSums = new ArrayList<Double>(); // sums of E(TiTj) from each sample.
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
		// Compute E(TiTj) values from the above tree and add them to ones in the list //
		computeETiTj(root, ETiTjSums, samplecounter);
		samplecounter++;
	    }
	}
	// Divide sums of E(T_i*T_j) values by number of samples to get average //
	for (int i = 0; i < ETiTjSums.size(); i++) {
	    double sum = ETiTjSums.get(i).doubleValue();
	    double average = sum / samplecounter;
	    // Report this average //
	    System.out.printf("%.34f", average);
	    System.out.println();
	}
    }

    /** Calculates E(TiTj) values from a TreeNode representing the ROOT of a
     *  coalescence tree and adds them to SUM_LIST.  First determines whether
     *  this list is empty by checking the COUNTER. */
    public static void computeETiTj(TreeNode root, ArrayList<Double> sum_list, double counter) {
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
	// Use this list to compute E(TiTj) values and them to the list of sums //
	updateList(intercoalescence_times, sum_list, counter);
    }

    /** Given a list of INTERCOALESCENCE_TIMES and COUNTER, updates list of 
     *  current SUMS of E(TiTj) values. */
    public static void updateList(ArrayList<Double> intercoalescence_times,
				  ArrayList<Double> sums, double counter) {
	if (counter == 0) {
	    buildList(intercoalescence_times, sums);
	} else {
	    int index = 0; // keeps track of which ETiTj value we are computing.
	    for (int i = 0; i < intercoalescence_times.size(); i++) {
		double time_i = intercoalescence_times.get(i).doubleValue();
		for (int j = 0; j < intercoalescence_times.size(); j++) {
		    double time_j = intercoalescence_times.get(j).doubleValue();
		    double TiTj = time_i * time_j;
		    double currentsum = sums.get(index).doubleValue();
		    double newsum = currentsum + TiTj;
		    sums.set(index, new Double(newsum));
		    index++;
		}
	    }
	}
    }

    /** Similar to updateList method, except that list of INTERCOALESCENCE_TIMES is
     *  is used to add to list of SUMS rather than update it. */
    public static void buildList(ArrayList<Double> intercoalescence_times, ArrayList<Double> sums) {
	for (int i = 0; i < intercoalescence_times.size(); i++) {
	    double time_i = intercoalescence_times.get(i).doubleValue();
	    for (int j = 0; j < intercoalescence_times.size(); j++) {
		double time_j = intercoalescence_times.get(j).doubleValue();
		double TiTj = time_i * time_j;
		sums.add(new Double(TiTj));
	    }
	}
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
