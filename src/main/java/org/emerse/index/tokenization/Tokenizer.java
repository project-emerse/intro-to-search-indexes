package org.emerse.index.tokenization;

import org.emerse.index.TokenStream;

public interface Tokenizer
{
	TokenStream tokenize(String text);
}
