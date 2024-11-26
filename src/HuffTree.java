
import java.util.HashMap;

public class HuffTree implements IHuffConstants {
	private TreeNode root;
	private int size;

	/**
	 * constructor of Huff Tree based on the frequencies of each alpha size
	 * 
	 * @param fre - the array of frequencies
	 */
	public HuffTree(int[] fre) {
		TreeNode[] freq = new TreeNode[ALPH_SIZE];
		// goes through frequency array adding non zero frequencies to the array that
		// will become to the tree
		for (int i = 0; i < fre.length; i++) {
			if (fre[i] != 0) {
				freq[i] = new TreeNode(i, fre[i]);
				size++;
			}
		}

		// adding all values to priority queue
		PriorityQueue<TreeNode> con = new PriorityQueue<TreeNode>(freq);
		con.enqueue(new TreeNode(PSEUDO_EOF, 1));
		size++;

		// making tree
		while (con.size() >= 2) {
			TreeNode temp = new TreeNode(con.dequeue(), -1, con.dequeue());
			con.enqueue(temp);
		}
		root = con.dequeue();

	}

	/**
	 * contructor for Huff Tree based of a root Tree Node
	 * 
	 * @param root - the Tree Node to be made the root
	 */
	public HuffTree(TreeNode root) {
		this.root = root;
	}

	/**
	 * returns the number of leaf nodes in the HuffTree
	 * 
	 * @return size of HuffTree - number of nodes
	 */
	public int getSize() {
		return size;
	}

	/**
	 * creates the map of original encoding to huffman compressed encoding
	 * 
	 * @return - the unordered map of the new encodings
	 */
	public HashMap<Integer, String> makeMap() {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		preOrderTrav(map, root, "");
		return map;

	}

	/**
	 * pre order traversal of the huffman tree to create the huffman compressed
	 * encodings and putting them in a map with the key as the original encoding
	 * 
	 * @param map  - the map to add the huffman encodings to
	 * @param n    - the current tree node
	 * @param code - the current huffman encoding
	 */
	private void preOrderTrav(HashMap<Integer, String> map, TreeNode n, String code) {
		if (n != null) {
			if (n.isLeaf()) {
				map.put(n.getValue(), code);
				code = "";
			}
			// recursive method call
			preOrderTrav(map, n.getLeft(), code + "0");
			preOrderTrav(map, n.getRight(), code + "1");

		}
	}

	/**
	 * making map for decompression of huffman encoding to original encoding
	 * 
	 * @return - the map for decompression
	 */
	public HashMap<String, Integer> makeMapD() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		preOrderTravD(map, root, "");
		return map;
	}

	/**
	 * pre order traversal of the huffman tree to create the huffman compressed
	 * encodings and putting them in a map with the key as the huffman encodings and
	 * the value as the original encodings
	 * 
	 * @param map  - the map to add the original and huffman encodings to
	 * @param n    - the current tree node
	 * @param code - the current huffman encoding
	 */
	private void preOrderTravD(HashMap<String, Integer> map, TreeNode n, String code) {
		if (n != null) {
			if (n.isLeaf()) {
				map.put(code, n.getValue());
				code = "";
			}
			preOrderTravD(map, n.getLeft(), code + "0");
			preOrderTravD(map, n.getRight(), code + "1");
		}
	}

	/**
	 * returns the root of the huffman tree
	 * 
	 * @return - the root
	 */
	public TreeNode getRoot() {
		return root;
	}

}
