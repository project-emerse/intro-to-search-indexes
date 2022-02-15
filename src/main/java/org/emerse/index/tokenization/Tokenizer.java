package org.emerse.index.tokenization;

import org.emerse.index.TokenStream;

/**
 * A tokenizer produces a {@link TokenStream} from text.  In a more realistic system, rather than
 * passing a {@link String}, you would probably get a {@link java.io.Reader} because the input could
 * be very large, and efficiently tokenized in chunks.
 */
public interface Tokenizer
{
	TokenStream tokenize(String text);
}
