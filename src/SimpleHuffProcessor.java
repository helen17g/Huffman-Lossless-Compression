
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class SimpleHuffProcessor implements IHuffProcessor {
	
	private IHuffViewer myViewer; // GUI 
	private HashMap<Integer, String> map; // map of ASCII code to compress encoding  
	private int headerFormat; // count or tree 
	private int[] freq; // frequencies of each character in the original file 
	private HuffTree tr; // huffman tree created 
	private int newTotalBits; // number of bits in compressed file 
	private int original; // number of bits in original file 

	/**
	 * Preprocess data so that compression is possible --- count characters/create
	 * tree/store state so that a subsequent call to compress will work. The
	 * InputStream is <em>not</em> a BitInputStream, so wrap it int one as needed.
	 * 
	 * @param in           is the stream which could be subsequently compressed
	 * @param headerFormat a constant from IHuffProcessor that determines what kind
	 *                     of header to use, standard count format, standard tree
	 *                     format, or possibly some format added in the future.
	 * @return number of bits saved by compression or some other measure Note, to
	 *         determine the number of bits saved, the number of bits written
	 *         includes ALL bits that will be written including the magic number,
	 *         the header format number, the header to reproduce the tree, AND the
	 *         actual data.
	 * @throws IOException if an error occurs while reading from the input file.
	 */
	public int preprocessCompress(InputStream in, int headerFormat) throws IOException {

		BitInputStream bits = new BitInputStream(in); // initializing bit input stream for file
		if (headerFormat != STORE_COUNTS && headerFormat != STORE_TREE) {
			myViewer.showError("Header format must be a count of frequencies or a tree");
			
		}
		this.headerFormat = headerFormat;

		// saving values of pre-compression processes with huffman coding algorithm
		freq = frequency(bits);
		tr = new HuffTree(freq);
		map = tr.makeMap();

		// calculating number of bits saved
		original = originalBits();
		newTotalBits = newBits();
		return original - newTotalBits;
	}

	/**
	 * calculates number of bits in original file (before compression) using freq
	 * array
	 * 
	 * @return number of bits in original file
	 */
	private int originalBits() {
		int total = 0;
		// iterates through frequency array to sum number of total number of items to be
		// coded
		for (int i = 0; i < freq.length; i++) {
			total += freq[i];
		}
		return total * BITS_PER_WORD;
	}

	/**
	 * Calculates number of bits expected in compressed file, based on header type
	 * 
	 * @return number of bits expected in compressed file
	 */
	private int newBits() {
		int total = BITS_PER_INT * 2; // magic number + header format
		// header length for counts format
		if (headerFormat == STORE_COUNTS) {
			total += ALPH_SIZE * BITS_PER_INT;
			// header length for tree format
		} else if (headerFormat == STORE_TREE) {
			total += BITS_PER_INT; // size of the tree container bits
			total += (tr.getSize()-1 + map.size() * (BITS_PER_WORD + 2)); // size of tree encoding
		}
		// calculates number of bits for encoding the actual file
		for (int i = 0; i < freq.length; i++) {
			if (freq[i] != 0) {
				// finds length of code for i'th character in freq
				int tempLen = map.get(i).length();
				total += freq[i] * tempLen;
			}
		}
		total += map.get(IHuffConstants.PSEUDO_EOF).length(); // adds number of bits for PEOF
		return total;
	}

	/**
	 * creates frequency table for all 'characters' in file
	 * 
	 * @param bits - InputStream for file
	 * @return array containing frequencies of each 'character' in file
	 * @throws IOException
	 */
	private int[] frequency(BitInputStream bits) throws IOException {
		int[] freq = new int[ALPH_SIZE];
		int inbits = bits.readBits(BITS_PER_WORD);
		// reads file in increments until no bits are left
		while (inbits != -1) {
			freq[inbits]++; // increments array at corresponding integer value
			inbits = bits.readBits(BITS_PER_WORD);
		}
		return freq;
	}

	/**
	 * Compresses input to output, where the same InputStream has previously been
	 * pre-processed via <code>preprocessCompress</code> storing state used by this
	 * call. <br>
	 * pre: <code>preprocessCompress</code> must be called before this method
	 * 
	 * @param in    is the stream being compressed (NOT a BitInputStream)
	 * @param out   is bound to a file/stream to which bits are written for the
	 *              compressed file (not a BitOutputStream)
	 * @param force if this is true create the output file even if it is larger than
	 * 
	 *              the input file. If this is false do not create the output file
	 *              if it is larger than the input file.
	 * @return the number of bits written.
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
		if (force || newTotalBits < original) { // if force enabled or if compressed file smaller
			Compress c = new Compress();
			return c.compress(in, out, tr, map, freq, headerFormat, newTotalBits);
		}
		myViewer.showError("Compressed file has " + (newTotalBits - original) + " more bits than "
				+ "the original. Select force to compress.");
		return 0; // nothing compressed
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out.
	 * 
	 * @param in  is the previously compressed data (not a BitInputStream)
	 * @param out is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int uncompress(InputStream in, OutputStream out) throws IOException {
		Uncompress uc = new Uncompress();
		return uc.decompress(in, out, myViewer);
	}

	public void setViewer(IHuffViewer viewer) {
		myViewer = viewer;
	}

	private void showString(String s) {
		if (myViewer != null) {
			myViewer.update(s);
		}
	}
}
