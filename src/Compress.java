

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class Compress implements IHuffConstants {

	/**
	 * method to actaully compress the file and write the huffman encodings to the
	 * out file and returning the amount of bits written to the compressed file
	 * 
	 * @param in           - the original file to compress
	 * @param out          - the file to write the compressed bits into
	 * @param tr           - the huffman tree for the in file
	 * @param map          - the map of huffman values keyed to there orginal bits
	 * @param freq         - the frequencies of each possible collection of bits
	 *                     from the original file
	 * @param headerFormat - the header format either counts or tree
	 * @param newTotalBits - the amount of bits that should be written
	 * @return - the amount of bits written
	 * @throws IOException
	 */
	public int compress(InputStream in, OutputStream out, HuffTree tr, HashMap<Integer, String> map,
			int[] freq, int headerFormat, int newTotalBits) throws IOException {
		BitInputStream bits = new BitInputStream(in);
		BitOutputStream bitsOut = new BitOutputStream(out);
		bitsOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
		// if headerformat is counts write in the frequencies in the compression file
		if (headerFormat == STORE_COUNTS) {
			bitsOut.writeBits(BITS_PER_INT, STORE_COUNTS);
			for (int k = 0; k < ALPH_SIZE; k++) {
				bitsOut.writeBits(BITS_PER_INT, freq[k]);
			}
			// if the header format is tree writing the tree into the compressed file
		} else if (headerFormat == STORE_TREE) {
			bitsOut.writeBits(BITS_PER_INT, STORE_TREE);
			int size = tr.getSize() - 1 + (BITS_PER_WORD + 2) * map.size();
			bitsOut.writeBits(BITS_PER_INT, size);
			preOrderTravMain(bitsOut, tr.getRoot());
		}
		// compressing the data
		int inbits = bits.readBits(BITS_PER_WORD);
		while (inbits != -1) {
			String s = map.get(inbits);
			bitWriter(bitsOut, s);
			inbits = bits.readBits(BITS_PER_WORD);
		}
		// adding the PEOF to the compressed file
		String pseudo = map.get(PSEUDO_EOF);
		bitWriter(bitsOut, pseudo);
		bitsOut.close();
		return newTotalBits;
	}

	/**
	 * goes through a string of the huffman encoding for a value bit by bit
	 * 
	 * @param bitsOut - the file to write the compressed values to
	 * @param s       - the huffman encoding
	 */
	private void bitWriter(BitOutputStream bitsOut, String s) {
		// goes through the huffman encoding adding bit by bit to compressed file
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '0') {
				bitsOut.writeBits(1, 0);
			} else {
				bitsOut.writeBits(1, 1);
			}
		}
	}

	/**
	 * goes through a preorder traversal of the huffman tree writing the tree into
	 * the compressed file
	 * 
	 * @param bitsOut - the file to write the huffman tree into
	 * @param n       - the current node
	 */
	private void preOrderTravMain(BitOutputStream bitsOut, TreeNode n) {
		if (n != null) {
			// if its a leaf node write the pre value 1 and then the huffman encoding for
			// that node
			if (n.isLeaf()) {
				bitsOut.writeBits(1, 1);
				bitsOut.writeBits(BITS_PER_WORD + 1, n.getValue());
				// else for internal node a zero to represent it
			} else {
				bitsOut.writeBits(1, 0);
			}
			preOrderTravMain(bitsOut, n.getLeft());
			preOrderTravMain(bitsOut, n.getRight());
		}
	}

}
