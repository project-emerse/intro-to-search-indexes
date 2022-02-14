package org.emerse.index.query;

import org.emerse.index.Index;
import org.emerse.index.Posting;

import java.util.*;

public class TokenQuery implements Query
{
	private final String term;
	private List<Posting> postings;
	private int postingIndex;
	private int positionsIndex;

	public TokenQuery(String term)
	{
		this.term = term;
	}

	@Override
	public void execute(Index index)
	{
		postings = index.lookup(term);
		postingIndex = -1;
	}

	@Override
	public boolean nextDoc()
	{
		positionsIndex = -1;
		return ++positionsIndex < postings.size();
	}

	@Override
	public int docId()
	{
		return postings.get(postingIndex).docId;
	}

	@Override
	public boolean nextSpan()
	{
		var positions = postings.get(postingIndex).positions;
		return ++positionsIndex < positions.size();
	}

	@Override
	public int startPosition()
	{
		return postings.get(postingIndex).positions.get(positionsIndex);
	}

	@Override
	public int endPosition()
	{
		return startPosition() + 1;
	}

	@Override
	public int startOffset()
	{
		return postings.get(postingIndex).offsets.get(positionsIndex * 2);
	}

	@Override
	public int endOffset()
	{
		return postings.get(postingIndex).offsets.get(positionsIndex * 2 + 1);
	}
}
