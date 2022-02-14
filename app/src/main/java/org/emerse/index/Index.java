package org.emerse.index;

import java.util.*;

/**
 * An index is basically a map from a token to a list of documents that token occurs in, plus for
 * each such document, a list of positions in that document that the token appears. All this
 * information related to a single document for a specific token is wrapped up into what is called a
 * "posting" in the information retrieval literature.  Thus, an index is a map from tokens to a list
 * of postings.
 * <p>
 * Thus, the only operation an index supports is to retrieve the postings list for any given token.
 * All {@link org.emerse.index.query.Queries queries} are built on top of this single operation.
 */
public class Index
{
	private final Map<String, List<Posting>> termToPostingsList = new HashMap<>();
	private int nextDocId;

	public int index(TokenStream stream)
	{
		int docId = nextDocId++;
		var termToDocPosting = new HashMap<String, Posting>();
		while (stream.next())
		{
			var postings = termToDocPosting.computeIfAbsent(stream.text(), x -> new Posting(docId));
			postings.positions.add(stream.position());
			postings.offsets.add(stream.startOffset());
			postings.offsets.add(stream.endOffset());
		}
		termToDocPosting.forEach((term, postings) -> termToPostingsList
			.computeIfAbsent(term, x -> new ArrayList<>())
			.add(postings));
		return docId;
	}

	public List<Posting> lookup(String term)
	{
		return termToPostingsList.get(term);
	}
}
