package org.emerse.index.query;

import org.emerse.index.Index;

/**
 * A query executes against an index, and produces a series of documents that match the query, and
 * (optionally) spans of tokens that match the query.  You don't have to iterate through the spans
 * to advance to the next document; you can advance to the next document at any time.
 * <p>
 * Calling {@link #nextDoc()} will return true if there is a next doc, and then set that document as
 * the "current" document.  This allows additional methods to be called that require a current
 * document:
 * <ul>
 *   <li>{@link #docId()} - to get the document id</li>
 *   <li>{@link #nextSpan()} - to start iteration through the matching spans of the current
 *   document</li>
 * </ul>
 * <p>
 * Similarly, calling {@link #nextSpan()} will return true if there is a next span for the current
 * document.  This sets the current span, which allows additional methods to be called
 * querying the current span:
 * <ul>
 *   <li>{@link #startPosition()}</li>
 *   <li>{@link #endPosition()}</li>
 *   <li>{@link #startOffset()}</li>
 *   <li>{@link #endOffset()}</li>
 * </ul>
 * The start/end pairs of methods define a closed-open interval of positions of characters.
 * That is, a position or offset {@code i} is in the interval iff
 * {@code startXXX() <= i < endXXX()}.
 *
 * <p>
 * Usage:
 * <pre>{@code
 *  Query q = ...;
 *  q.execute(index);
 *  while (q.nextDoc())
 *  {
 *    // q.docId() is a document id that matches the query `q`
 *    // You do not have to iterate through all spans if you don't want to
 *    // You can also partially iterate through the positions then move to the next document.
 *    while (q.nextSpan())
 *    {
 *      // The terms between q.startPosition() and q.endPosition() match the query.
 *      // The span as character offsets into the original text are found
 *      // with q.startOffset() and q.endOffset().
 *    }
 *  }
 * }</pre>
 */
public interface Query
{
	void execute(Index index);


	/**
	 * It is an error to call this method before {@link #execute(Index)} is called, or after it has
	 * returned false for a particular execution.
	 *
	 * @return true if there is a next matching document
	 */
	boolean nextDoc();

	/**
	 * It is an error to call this method unless {@link #nextDoc()} returned true.
	 *
	 * @return the current document id
	 */
	int docId();



	/**
	 * It is an error to call this method unless {@link #nextDoc()} returned true. It is also an error
	 * to call this method after it has returned false for a particular doc.
	 *
	 * @return true if there is a next matching position
	 *
	 * @throws UnsupportedOperationException
	 * 	if this operation is not supported by this query
	 */
	boolean nextSpan();

	/**
	 * It is an error to call this method unless {@link #nextSpan()} returned true.
	 *
	 * @return the first token position of the current matching span
	 */
	int startPosition();

	/**
	 * It is an error to call this method unless {@link #nextSpan()} returned true.
	 *
	 * @return the one past the last token position of the current matching span
	 */
	int endPosition();

	/**
	 * It is an error to call this method unless {@link #nextSpan()} returned true.
	 *
	 * @return the first character offset of the matching span in the original text
	 */
	int startOffset();

	/**
	 * It is an error to call this method unless {@link #nextSpan()} returned true.
	 *
	 * @return one past the last character offset of the matching span in the original text
	 */
	int endOffset();
}
