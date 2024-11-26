
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class Uncompress implements IHuffConstants {

	// instance variables
	private int[] freq;
	private HuffTree tr;
	private Map<String, Integer> map;

	/**
	 * preforms the actual decompression from the huffman file to the original file
	 * 
	 * @param in       - huffman file
	 * @param out      - file to write orginal file contents into
	 * @param myViewer - GUI
	 * @return - number of bits written to decompressed file
	 * @throws IOException
	 */
	public int decompress(InputStream in, OutputStream out, IHuffViewer myViewer)
			throws IOException {
		BitInputStream bitsIn = new BitInputStream(in);
		int magicNumber = bitsIn.readBits(BITS_PER_INT);
		// if not a huffman file throw error
		if (magicNumber != MAGIC_NUMBER) {
			myViewer.showError("Not a Huffman file.");
			bitsIn.close();
			return -1;
		}
		int headerFormat = bitsIn.readBits(BITS_PER_INT);
		// if header is in the counts format collect frequencies
		if (headerFormat == STORE_COUNTS) {
			freq = new int[256];
			for (int k = 0; k < ALPH_SIZE; k++) {
				int frequencyInOriginalFile = bitsIn.readBits(BITS_PER_INT);
				freq[k] = frequencyInOriginalFile;
			}
			tr = new HuffTree(freq);
			// if header is in tree format make huffman tree
		} else if (headerFormat == STORE_TREE) {
			bitsIn.readBits(BITS_PER_INT);
			TreeNode root = treeHelper(bitsIn);
			tr = new HuffTree(root);
		}
		map = tr.makeMapD();
		BitOutputStream bitsOut = new BitOutputStream(out);
		// do the actual writing to decompress file
		return writingToDFile(bitsIn, bitsOut, tr.getRoot(), 0);
	}

	/**
	 * writes to the decompressed file the bits and counts the bits it writes
	 * 
	 * @param bitsIn  - the huffman file to read
	 * @param bitsOut - the file to write decompressed values to
	 * @param n       - the root of the tree
	 * @param s       - the current huffman code string to convert
	 * @return - bits written to file
	 * @throws IOException
	 */
	private int writingToDFile(BitInputStream bitsIn, BitOutputStream bitsOut, TreeNode n,
			int totalBits) throws IOException {
		int inbits = bitsIn.readBits(1);
		String s = "";
		boolean done = false;
		// while there is huffman file to read and no PEOF found
		while (inbits != -1 && !done) {
			// if leaf node found write its original value to decompressed file or if PEOF
			// end file reading
			if (n.isLeaf()) {
				if (n.getValue() == IHuffConstants.PSEUDO_EOF) {
					System.out.println("WORDS");
					done = true;
				} else {
					bitsOut.writeBits(BITS_PER_WORD, map.get(s));
					totalBits += BITS_PER_WORD;
					s = "";
					n = tr.getRoot();
				}
			}
			// if current inbit is a 1 go write in the tree
			if (inbits == 1) {
				s += "1";
				n = n.getRight();
			} else {
				// if current inbit is 0 go left in the tree
				s += "0";
				n = n.getLeft();
			}
			if (!done) {
				inbits = bitsIn.readBits(1);
			}
		}

		bitsIn.close();
		bitsOut.close();
		// total bits written to the decompressed file
		return totalBits;
	}

	/**
	 * recursive helper method to help reconstruct the huffman encoded tree
	 * 
	 * @param bitsIn - the huffman encoded file
	 * @return - the root of the huffman tree
	 * @throws IOException
	 */
	private TreeNode treeHelper(BitInputStream bitsIn) throws IOException {
		int currBit = bitsIn.readBits(1);
		if (currBit == 0) {
			TreeNode newN = new TreeNode(-1, 0);
			newN.setLeft(treeHelper(bitsIn));
			newN.setRight(treeHelper(bitsIn));
			return newN;
		} else if (currBit == 1) {
			int moreBits = bitsIn.readBits(BITS_PER_WORD + 1);
			TreeNode newN = new TreeNode(moreBits, 0);
			return newN;
		} else {
			throw new IOException("catastrophe");
		}
	}
}
