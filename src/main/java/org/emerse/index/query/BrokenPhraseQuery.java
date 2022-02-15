package org.emerse.index.query;

public class BrokenPhraseQuery extends ConjunctionQuery
{
	public BrokenPhraseQuery(Query left, Query right)
	{
		super(left, right);
	}

	@Override
	public boolean nextSpan()
	{
		var leftSpanEnd = -2;
		var rightSpanStart = -1;
		while (leftSpanEnd != rightSpanStart)
		{
			while (leftSpanEnd < rightSpanStart)
			{
				if (!left.nextSpan())
				{
					return false;
				}
				leftSpanEnd = left.endPosition();
			}
			while (rightSpanStart < leftSpanEnd)
			{
				if (!right.nextSpan())
				{
					return false;
				}
				rightSpanStart = right.startPosition();
			}
		}
		return true;
	}

	@Override
	public int startPosition()
	{
		return left.startPosition();
	}

	@Override
	public int endPosition()
	{
		return right.endPosition();
	}

	@Override
	public int startOffset()
	{
		return left.startOffset();
	}

	@Override
	public int endOffset()
	{
		return right.endOffset();
	}
}
