package org.emerse.index.query;

import org.emerse.index.Index;

public class ConjunctionQuery implements Query
{
	protected final Query left;
	protected final Query right;

	public ConjunctionQuery(Query left, Query right)
	{
		this.left = left;
		this.right = right;
	}

	@Override
	public void execute(Index index)
	{
		left.execute(index);
		right.execute(index);
	}

	@Override
	public boolean nextDoc()
	{
		var leftDocId = -2;
		var rightDocId = -1;
		while (leftDocId != rightDocId)
		{
			while (leftDocId < rightDocId)
			{
				if (!left.nextDoc())
				{
					return false;
				}
				leftDocId = left.docId();
			}
			while (rightDocId < leftDocId)
			{
				if (!right.nextDoc())
				{
					return false;
				}
				rightDocId = right.docId();
			}
		}
		return true;
	}

	@Override
	public int docId()
	{
		return left.docId();
	}

	@Override
	public boolean nextSpan()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int startPosition()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int endPosition()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int startOffset()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int endOffset()
	{
		throw new UnsupportedOperationException();
	}
}
