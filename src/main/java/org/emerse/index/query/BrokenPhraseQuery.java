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
		var leftPos = -2;// the end position of the left span
		var rightPos = -1;// the start position of right span
		while (leftPos != rightPos)
		{
			while (leftPos < rightPos)
			{
				if (!left.nextSpan())
				{
					return false;
				}
				leftPos = left.endPosition();
			}
			while (rightPos < leftPos)
			{
				if (!right.nextSpan())
				{
					return false;
				}
				rightPos = right.startPosition();
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
