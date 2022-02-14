package org.emerse.index.query;

import org.emerse.index.Index;

public class BrokenNoOverlapQuery implements Query
{
	private final Query base;
	private final Query exclude;
	private boolean excludeHasMoreSpans = false;
	private int excludedDocId = -1;
	private int excludeSpanStart;
	private int excludeSpanEnd;

	public BrokenNoOverlapQuery(Query base, Query exclude)
	{
		this.base = base;
		this.exclude = exclude;
	}

	@Override
	public void execute(Index index)
	{
		base.execute(index);
		exclude.execute(index);
	}

	@Override
	public boolean nextDoc()
	{
		if (!base.nextDoc())
		{
			return false;
		}
		while (excludedDocId < base.docId())
		{
			if (!exclude.nextDoc())
			{
				excludedDocId = Integer.MAX_VALUE;
			}
		}
		excludeHasMoreSpans = excludedDocId == base.docId();
		excludeSpanStart = -2;
		excludeSpanEnd = -1;
		return true;
	}

	@Override
	public int docId()
	{
		return base.docId();
	}

	@Override
	public boolean nextSpan()
	{
		if (!base.nextSpan())
		{
			return false;
		}
		var changed = true;
		while (changed)
		{
			changed = false;
			// while exclude and base overlap, advance base
			while (!(excludeSpanEnd <= base.startPosition() || base.endPosition() <= excludeSpanStart))
			{
				if (!base.nextSpan())
				{
					return false;
				}
				changed = true;
			}
			// while exclude is before base, advance exclude
			while (excludeHasMoreSpans && excludeSpanEnd <= base.startPosition())
			{
				if (!exclude.nextSpan())
				{
					excludeHasMoreSpans = false;
					break;
				}
				excludeSpanStart = exclude.startPosition();
				excludeSpanEnd = exclude.endPosition();
				changed = true;
			}
		}
		return true;
	}

	@Override
	public int startPosition()
	{
		return base.startPosition();
	}

	@Override
	public int endPosition()
	{
		return base.endPosition();
	}

	@Override
	public int startOffset()
	{
		return base.startOffset();
	}

	@Override
	public int endOffset()
	{
		return base.endOffset();
	}
}
