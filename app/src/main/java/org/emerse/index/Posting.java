package org.emerse.index;

import java.util.*;

/**
 * A posting is associated with a particular token, and describes where that token appears in a
 * specific document.
 */
public class Posting
{
	/**
	 * The document the token appears in.
	 */
	public int docId;

	/**
	 * The positions of the token in the document.
	 */
	public List<Integer> positions = new ArrayList<>();

	/**
	 * The offsets into the original text for each occurrences of the token in the document.
	 * Specifically, the offsets for the occurrence token at position {@code positions.get(i)} is from
	 * {@code offsets.get(i * 2)} to {@code offsets.get(i * 2 + 1)}.
	 */
	public List<Integer> offsets = new ArrayList<>();

	public Posting(int docId)
	{
		this.docId = docId;
	}
}
