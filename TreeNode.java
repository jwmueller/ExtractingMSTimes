/** Class for the nodes of coalescence tree described in Newick format.
 *  @author Jonas Mueller
 */

public class TreeNode {

    /** The left child of this node. */
    TreeNode left;
    
    /** The right child of this node. */
    TreeNode right;
    
    /** The parent of this node. */
    TreeNode parent;

    /** The length of the branch coming out of the top of this node. */
    double branch_length;

    /** The time (measured in units of 4(N_0) generations) of 
     *  the coalescence represented in this node. */
    double coalescence_time;
}