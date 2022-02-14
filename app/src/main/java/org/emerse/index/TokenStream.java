package org.emerse.index;

/**
 * A token stream is an iterator of tokens.  Calling {@link #next()} will advance to the next token
 * in the stream making it the "current" token, returning false if there are no more tokens. Once
 * the current token is set, a number of methods can be used to find the various parts of a token.
 * <p>
 * A token consists of
 * <ul>
 *   <li>the text of the token, reported by {@link #text()}</li>
 *   <li>the position of the token, reported by {@link #position()}</li>
 *   <li>
 *     the span of offsets of the token into the original text,
 *     reported by {@link #startOffset()} and {@link #endOffset()}
 *   </li>
 * </ul>
 */
public interface TokenStream
{
	/**
	 * Advance to the next token, making it the current token.  This must be called to advance to the
	 * first token as well.
	 *
	 * @return true if there is a next token
	 */
	boolean next();



	/**
	 * It is an error to call this if {@link #next()} returned false.
	 *
	 * @return the text of the current token
	 */
	String text();

	/**
	 * It is an error to call this if {@link #next()} returned false. Positions are non-decreasing.
	 *
	 * @return the position of the current token in the stream
	 */
	int position();

	/**
	 * It is an error to call this if {@link #next()} returned false.
	 *
	 * @return the start offset of the current token in the stream
	 */
	int startOffset();

	/**
	 * It is an error to call this if {@link #next()} returned false.
	 *
	 * @return the end offset of the current token in the stream
	 */
	int endOffset();
}
