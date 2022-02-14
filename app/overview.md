<script>
let style = document.createElement('style');
style.append(document.createTextNode(`
.basic { 
  border-collapse: collapse;
  border: 1px solid black;
  margin-top: 0.5em;
}
.basic th, .basic td {
  border: 1px solid black;
  padding: 0.2em;
}
`));
document.head.append(style);
</script>

There are three major processes related to a search index:

1. tokenization,
2. indexing, and
3. querying.

The goal of indexing is to do full-text search extremely quickly. Indexes do
this by storing the results of the simplest query directly: the query for a
single token (ie, word). All other queries are built from this basic operation,
and so are similarly fast, though some additional structure is needed to keep
more complex queries fast.

# Tokenization

Tokenization is the process of converting the text of the document into a series
of tokens. Tokens are the only thing the index sees and knows about, which means
they are the only thing that can actually be searched for. Because of this,
there is usually processing between the query as the user might type it in, and
the actual tokens sent to the index during search. Because of this processing,
we can make the structure of tokens in a document quite rich.

To explore what we can do with tokens, let's start with a simple example.
Suppose we want to tokenize the phrase: "Patient denies chest pain."
A simple word-based tokenizer may produce a token stream such as:

<table class="basic">
<caption><i>"Patient denies chest pain"</i></caption>
<thead>
<tr><th>Token Text</th><th>Position</th><th>Offsets</th></tr>
</thead>
<tbody>
<tr><td>Patient</td> <td>1</td> <td>0-7  </td></tr>
<tr><td>denies </td> <td>2</td> <td>8-14 </td></tr>
<tr><td>chest  </td> <td>3</td> <td>15-20</td></tr>
<tr><td>pain   </td> <td>4</td> <td>21-25</td></tr>
</tbody>
</table>

<table class="basic">
<caption>Queries</caption>
<thead>
<tr><th>Query</th><th>Found Positions</th></tr>
</thead>
<tbody>
<tr><td>Patient</td><td>1</td></tr>
<tr><td>patient</td><td><i>no results</i></td></tr>
<tr><td>patients</td><td><i>no results</i></td></tr>
</tbody>
</table>

As we can see, a token has three parts: the token text, which is how we identify
the token, its position and the offsets into the source text. If we were to
index this token stream, and look up the term "Patient" it would tell us that
word appeared at position 1 of this document.  (It's implicit that all these
tokens would be indexed under the document id under which they came.)
However, if we lookup up the token "patient" (all lowercase), no results would
come back (for this document anyway). This is because the token text is
case-sensitive. Often this is not desired (though it could be to match
abbreviations more accurately). In addition, looking up a token like "Patients"
or "patients" also would return nothing, which is also often undesirable. For
this reason, text is often "normalized", meaning transformed into a more
standard form, often by lower-casing and stemming.

<table class="basic">
<caption><i>"The patients deny chest pain"</i></caption>
<thead>
<tr><th>Token Text</th><th>Position</th><th>Offsets</th></tr>
</thead>
<tbody>
<tr><td>the     </td> <td>1</td> <td>0-3  </td></tr>
<tr><td>patient </td> <td>2</td> <td>4-11 </td></tr>
<tr><td>deny    </td> <td>3</td> <td>12-16</td></tr>
<tr><td>chest   </td> <td>4</td> <td>17-22</td></tr>
<tr><td>pain    </td> <td>5</td> <td>23-27</td></tr>
</tbody>
</table>

<table class="basic">
<caption>Queries</caption>
<thead>
<tr><th>Query</th><th>Found Positions</th></tr>
</thead>
<tbody>
<tr><td>Patient</td><td><i>no results</i></td></tr>
<tr><td>patient</td><td>2</td></tr>
<tr><td>patients</td><td><i>no results</i></td></tr>
</tbody>
</table>

<table class="basic">
<caption>Queries with normalization</caption>
<thead>
<tr><th>Query</th><th>Normalized Query</th><th>Found Positions</th></tr>
</thead>
<tbody>
<tr><td>Patient </td><td>patient</td><td>2</td></tr>
<tr><td>patient </td><td>patient</td><td>2</td></tr>
<tr><td>patients</td><td>patient</td><td>2</td></tr>
</tbody>
</table>

Here, we can see the word "patients" produced the token "patient" and the word "
The" produced the token "the". Searching for "patient" (singular)
would return position 2, however searching for "patients" (plural)
would return nothing, since there is no token in the stream with the exact
text "patients". However, if we also normalize terms in the search query, the
query for "patients" would normalize to "patient", which _then_ we would look up
in the search index, and find it occurs at position 2.

# Phrase Queries

So far, we've only talked about querying for a single token, but there are
compound queries built out of this basic operation, such as phrase queries,
often written in double-quotes. Searching for the phrase "chest pain", the index
would first look up each individual word, then try to match the returned
positions to see if two positions are adjacent. The query then just doesn't
return a single position as a result, but a span of positions.

The way a phrase query matches can obviously be changed. Near-queries are much
like phrase queries, but rather than having a condition such as
`x + 1 = y`, they have a condition such as `|x - y| < 5` meaning the near-query
will match if the two words are within 5 positions. In Lucene/Solr, this is
written with the query syntax `"x y"~5`.

<table class="basic">
<caption>Queries on 
  <i>"The patients deny chest pain. One patient affirmed pain in the chest."</i>
</caption>
<thead>
<tr>
<th>Query</th>
<th>Terms in Query</th>
<th>Found Positions</th>
<th>Span Condition</th>
<th>Matching Spans</th>
</tr>
</thead>
<tbody>
<tr>
  <td rowspan="2">"chest pain"</td>
  <td>chest</td>
  <td>x &isin; {4, 12}</td>
  <td rowspan="2">{ (x, y) | x + 1 = y }</td>
  <td rowspan="2">(4,5)</td>
</tr>
<tr>
  <td>pain</td>
  <td>y &isin; {5, 9}</td>
</tr>
<tr>
  <td rowspan="3">"patient chest pain"</td>
  <td>patient</td>
  <td>x &isin; {2, 7}</td>
  <td rowspan="3">{ (x, y, z) | x + 1 = y &and; y + 1 = z }</td>
  <td rowspan="3"><i>no results</i></td>
</tr>
<tr>
  <td>chest</td>
  <td>y &isin; {4, 12}</td>
</tr>
<tr>
  <td>pain</td>
  <td>z &isin; {5, 9}</td>
</tr>
</tbody>
</table>

# Adding Metadata as Tokens

If near-queries can match tokens that are within some distance of one another
that distance could be zero, meaning the two tokens are at the _same_ position.
So far, we haven't generated any two tokens at the same position, so such a
capability in a query isn't useful yet, but there is no reason we can't produce
multiple tokens at the same position. This leads to a natural way of encoding
searchable metadata about tokens: encode such metadata as special tokens rooted
at the same position of the token they are describing.

