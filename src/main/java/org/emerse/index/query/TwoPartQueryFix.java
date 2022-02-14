package org.emerse.index.query;

import org.emerse.index.Index;

public class TwoPartQueryFix implements Query
{
	private final Query query;
	private boolean beforeFirstPosition;

	public TwoPartQueryFix(Query query)
	{
		this.query = query;
	}

	@Override
	public void execute(Index index)
	{
		query.execute(index);
	}

	@Override
	public boolean nextDoc()
	{
		beforeFirstPosition = true;
		while (true)
		{
			if (!query.nextDoc())
			{
				return false;
			}
			if (query.nextSpan())
			{
				return true;
			}
		}
	}

	@Override
	public int docId()
	{
		return query.docId();
	}

	@Override
	public boolean nextSpan()
	{
		if (beforeFirstPosition)
		{
			beforeFirstPosition = false;
			return true;
		}
		else
		{
			return query.nextSpan();
		}
	}

	@Override
	public int startPosition()
	{
		return query.startPosition();
	}

	@Override
	public int endPosition()
	{
		return query.endPosition();
	}

	@Override
	public int startOffset()
	{
		return query.startOffset();
	}

	@Override
	public int endOffset()
	{
		return query.endOffset();
	}
}
