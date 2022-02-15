package org.emerse.index.tokenization;

import org.emerse.index.TokenStream;

import java.util.regex.*;

public class SimpleWordTokenizer implements Tokenizer
{
	private static final Pattern WORD = Pattern.compile("\\w+");

	@Override
	public TokenStream tokenize(String text)
	{
		return new TokenStream()
		{
			int position;
			final Matcher matcher = WORD.matcher(text);

			@Override
			public boolean next()
			{
				position++;
				return matcher.find();
			}

			@Override
			public String text()
			{
				return matcher.group();
			}

			@Override
			public int position()
			{
				return position;
			}

			@Override
			public int startOffset()
			{
				return matcher.start();
			}

			@Override
			public int endOffset()
			{
				return matcher.end();
			}
		};
	}
}
