
import java.util.LinkedList;

/**
 * Generic PriorityQueue class where ties are broken by making element added
 * first have higher priority. Uses standard Java LinkedList as storage
 * container
 * 
 * @param <E>
 */
public class PriorityQueue<E extends Comparable<? super E>> {

	// storage container for priority queue
	private LinkedList<E> con;

	/**
	 * constructor for priority queue
	 * 
	 * @param freq - array of E to be added to queue
	 */
	public PriorityQueue(E[] freq) {
		con = new LinkedList<E>();
		// goes through the array of freq adding non nulls to the priority queue
		for (int i = 0; i < freq.length; i++) {
			if (freq[i] != null) {
				enqueue(freq[i]);
			}
		}
	}

	/**
	 * adds value to priority queue in correct position based on value
	 * 
	 * @param val - object to add to queue
	 */
	public void enqueue(E val) {
		int index = 0;
		// find index to add value at based on size of current queue and values of
		// existing indices
		while (index < con.size() && val.compareTo(con.get(index)) >= 0) {
			index++;
		}
		con.add(index, val);
	}

	/**
	 * returns the value at a specific index in the priority queue
	 * 
	 * @param val - int representing position in queue
	 * @return element at specified index in queue
	 */
	public E get(int val) {
		return con.get(val);

	}

	/**
	 * Removes element with highest priority from list (first element)
	 * 
	 * @return element removed
	 */
	public E dequeue() {
		return con.remove(0);
	}

	/**
	 * prints out priority queue (specifically w/ default linkedList to String)
	 */
	public String toString() {
		return con.toString();
	}

	/**
	 * @return size of priority queue
	 */
	public int size() {
		return con.size();
	}

}
