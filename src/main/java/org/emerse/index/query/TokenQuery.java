package org.emerse.index.query;

import org.emerse.index.Index;
import org.emerse.index.Posting;

import java.util.*;

public class TokenQuery implements Query
{
	private final String token;
	private List<Posting> postings;
	private int postingIndex;
	private int spanIndex;

	public TokenQuery(String token)
	{
		this.token = token;
	}

	@Override
	public void execute(Index index)
	{
		postings = index.lookup(token);
		postingIndex = -1;
	}

	@Override
	public boolean nextDoc()
	{
		spanIndex = -1;
		return ++postingIndex < postings.size();
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
		return ++spanIndex < positions.size();
	}

	@Override
	public int startPosition()
	{
		return postings.get(postingIndex).positions.get(spanIndex);
	}

	@Override
	public int endPosition()
	{
		return startPosition() + 1;
	}

	@Override
	public int startOffset()
	{
		return postings.get(postingIndex).offsets.get(spanIndex * 2);
	}

	@Override
	public int endOffset()
	{
		return postings.get(postingIndex).offsets.get(spanIndex * 2 + 1);
	}
}
