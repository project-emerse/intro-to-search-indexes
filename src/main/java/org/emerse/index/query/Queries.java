package org.emerse.index.query;

/**
 * All queries can be built on top of the {@link org.emerse.index.Index#lookup(String) index's basic
 * lookup operation}.  However, a query has {@link Query its own interface}, so that queries can be
 * built on top of other queries. That query interface basically says that a query matches a
 * document by containing one or more spans of tokens that match the query.  Thus, the query is
 * defined by which documents and spans it matches.  New queries can be built by combining these two
 * things from sub-queries in different ways.  This produces a kind of "algebra" out of the ways of
 * combining and constructing queries.  This algebra is presented as static methods of this class.
 * <p>
 * The simplest query is the {@link TokenQuery} which matches documents that contain the query, and
 * the spans are just the single tokens.  Note that both these things are stored in the {@link
 * org.emerse.index.Posting posting} attained from the {@link org.emerse.index.Index index}.
 * <p>
 * The {@link ConjunctionQuery conjunction} returns documents that match both queries that it's
 * built from.  It has no notion of "spans" that match.
 * <p>
 * The {@link BrokenPhraseQuery phrase} query matches spans built of out adjacent spans of the
 * sub-queries.
 * <p>
 * The {@link BrokenOverlapQuery overlap} query matches all spans of some base sub-query which
 * overlap spans of an augmenting sub-query.  The augmenting spans usually indicate some special
 * property of the spans of the base sub-query, such as being negated.
 * <p>
 * The phrase and overlap queries are technically "broken" since they report they match a document,
 * but then contain no matching spans in that document.  This is fixed by wrapping the query with
 * {@link TwoPartQueryFix}.  This query checks the {@link Query#nextSpan()} method to make sure any
 * document reported as matching really matches.  The methods of this class do this wrapping.
 */
public final class Queries
{
	private Queries()
	{
	}

	public static Query conjunction(Query left, Query right)
	{
		return new ConjunctionQuery(left, right);
	}

	public static Query token(String term)
	{
		return new TokenQuery(term);
	}

	public Query overlap(Query base, Query overlap)
	{
		return new TwoPartQueryFix(new BrokenOverlapQuery(base, overlap));
	}

	public Query noOverlap(Query base, Query exclude)
	{
		return new TwoPartQueryFix(new BrokenNoOverlapQuery(base, exclude));
	}

	public Query phrase(Query left, Query right)
	{
		return new TwoPartQueryFix(new BrokenPhraseQuery(left, right));
	}

	public Query phrase(Query... parts)
	{
		var l = parts.length;
		for (var halfSpan = 1; halfSpan < l; halfSpan *= 2)
		{
			var span = halfSpan * 2;
			for (int i = 0; i + halfSpan < l; i += span)
			{
				parts[i] = phrase(parts[i], parts[i + halfSpan]);
			}
		}
		return parts[0];
	}
}
