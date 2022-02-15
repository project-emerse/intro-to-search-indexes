<script>
let style = document.createElement('style');
style.append(document.createTextNode(`
table.basic { 
  border-collapse: collapse;
  border: 1px solid black;
  margin-top: 0.5em;
}
table.basic th, .basic td {
  border: 1px solid black;
  padding: 0.2em;
}
.comment {
  max-width: 30ex;
}
tr.no-td-border td {
  border: none;
}
table.basic td span {
  padding: 0.4em;
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
positions to see if two positions are adjacent. The query then returns a span of
positions instead of a single position.

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
  <td rowspan="3">
{ (x, y, z) | <span>x + 1 = y</span> 
&and; <span>y + 1 = z</span>
}</td>
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
<tr>
  <td rowspan="3">"patient chest pain"~5</td>
  <td>patient</td>
  <td>x &isin; {2, 7}</td>
  <td rowspan="3">
{ (x, y, z) | <span>|y - x| &lt; 5</span>
&and; <span>|z - y| &lt; 5</span> 
&and; <span>|z - x| &lt; 5</span>
}</td>
  <td rowspan="3">(2, 4 ,5), (7, 4, 5), (7, 4, 9), (7, 12, 9)</td>
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

The way a phrase query matches can obviously be changed. Near-queries are much
like phrase queries, but rather than having a condition such as
`x + 1 = y`, they have a condition such as `|y - x| < 5` meaning the near-query
will match if the two words are within 5 positions. In Lucene/Solr, this is
written with the query syntax `"x y"~5`.

# Adding Metadata as Tokens

If near-queries can match tokens that are within some distance of one another,
then that distance could be zero, meaning the two tokens are at the _same_
position. So far, we haven't generated two tokens at the same position, so such
a querying capability isn't useful yet, but there is no reason we can't produce
multiple tokens at the same position. This leads to a natural way of encoding
searchable metadata about tokens: encode such metadata as special tokens rooted
at the same position of the token they are describing.

<table class="basic">
<caption><i>"Patient denies chest pain."</i></caption>
<thead>
<tr><th>Token Text</th><th>Position</th><th>Offsets</th></tr>
</thead>
<tbody>
<tr><td>patient</td> <td>1</td> <td>0-7  </td></tr>
<tr><td>denies </td> <td>2</td> <td>8-14 </td></tr>
<tr><td>chest  </td> <td>3</td> <td>15-20</td></tr>
<tr><td>NEG    </td> <td>3</td> <td>15-20</td></tr>
<tr><td>pain   </td> <td>4</td> <td>21-25</td></tr>
<tr><td>NEG    </td> <td>4</td> <td>21-25</td></tr>
</tbody>
</table>

Here, we have output two additional tokens, positioned at the same place as the
words "chest pain" to indicate these words are negated. A search for
"chest" or "pain" would work as before, but we can devise another type of query
which would exclude words which are marked as negated. Lucene/Solr calls these
queries "span-not" queries, but has no default syntax to invoke them. I shall
make up the syntax `x - y` to indicate the use of such. Span-not queries can be
combined with phrase queries or near queries. The "span conditions" I wrote
below are hand-made to make sense for the intent of the query, but there are
methodical ways to execute such queries. See the {@link
org.emerse.index.query.Queries Queries} class for more detail.

<table class="basic">
<caption>Queries on <i>"Patient denies chest pain."</i></caption>
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
  <td rowspan="2">pain - NEG</td>
  <td>pain</td>
  <td>x &isin; {4}</td>
  <td rowspan="2">{ x | &not;&exist;y (x = y) }</td>
  <td rowspan="2"><i>no results</i></td>
</tr>
<tr>
  <td>NEG</td>
  <td>y &isin; {4, 5}</td>
</tr>
<tr>
  <td rowspan="3">"chest pain" - NEG</td>
  <td>chest</td>
  <td>x &isin; {4}</td>
  <td rowspan="3">{ (x, y) | <span>x + 1 = y</span>
&and; <span>&not;&exist;z (x = z &or; y = z)</span> }</td>
  <td rowspan="3"><i>no results</i></td>
</tr>
<tr>
  <td>pain</td>
  <td>y &isin; {5}</td>
</tr>
<tr>
  <td>NEG</td>
  <td>z &isin; {4,5}</td>
</tr>
</tbody>
</table>

There is one issue metadata-tokens: we need a reliable way to distinguish
metadata-tokens from tokens representing the text itself. Here, we assumed we
were lower-casing and stemming words to make tokens, so no word could ever end
up as the upper-case token NEG. However, in the real world, we would like to
also support case-sensitive search as well, in which case we cannot rely on the
case of tokens to distinguish them as metadata or not.

To solve this in general, we can prefix _all_ tokens with a kind of "class" or
"layer". We can have many "layers" to our tokens:

1. a case-sensitive layer, which preserves all information about the word
2. a case-insensitive layer, which lower-cases the word, and stems it
3. a negation layer, which contains an indication if the word is negated
4. an entity layer, which contains an indication if the word refers to an entity

Thus, we can visualize a tokenization of a sentence like so:

<table class="basic">
<caption>Visualization of tokenization of <i>"Patient denies chest pain"</i></caption>
<thead>
</thead>
<tbody>
<tr class="no-td-border">
<th></th>
<th>Sentence</th>
<td>Patient</td>
<td>denies</td>
<td>chest</td>
<td>pain</td>
</tr>
<tr>
<th rowspan="2">Common Token Attribues</th>
<th>Position</th>
<td>1</td>
<td>2</td>
<td>3</td>
<td>4</td>
</tr>
<tr>
<th>Offsets</th>
<td>0-7</td>
<td>8-14</td>
<td>15-20</td>
<td>21-25</td>
</tr>
<tr>
<th rowspan="4">Tokens</th>
<th>Case Sensitive</th>
<td>CS_Patient</td>
<td>CS_denies</td>
<td>CS_chest</td>
<td>CS_pain</td>
</tr>
<tr>
<th>Case Insensitive</th>
<td>CI_patient</td>
<td>CI_deny</td>
<td>CI_chest</td>
<td>CI_pain</td>
</tr>
<tr>
<th>Negation</th>
<td></td>
<td></td>
<td>N_NEG</td>
<td>N_NEG</td>
</tr>
<tr>
<th>ICD-10</th>
<td></td>
<td></td>
<td>ICD10_R07.9</td>
<td>ICD10_R07.9</td>
</tr>
</tbody>
</table>

<table class="basic">
<caption>Actual token stream of <i>"Patient denies chest pain"</i></caption>
<thead>
<tr><th>Token Text</th><th>Position</th><th>Offsets</th></tr>
</thead>
<tbody>
<tr><td>CS_Patient  </td> <td>1</td> <td>0-7  </td></tr>
<tr><td>CI_patient  </td> <td>1</td> <td>0-7  </td></tr>
<tr><td>CS_denies   </td> <td>2</td> <td>8-14 </td></tr>
<tr><td>CI_deny     </td> <td>2</td> <td>8-14 </td></tr>
<tr><td>CS_chest    </td> <td>3</td> <td>15-20</td></tr>
<tr><td>CI_chest    </td> <td>3</td> <td>15-20</td></tr>
<tr><td>N_NEG       </td> <td>3</td> <td>15-20</td></tr>
<tr><td>ICD10_R07.9 </td> <td>3</td> <td>15-20</td></tr>
<tr><td>CS_pain     </td> <td>4</td> <td>21-25</td></tr>
<tr><td>CI_pain     </td> <td>4</td> <td>21-25</td></tr>
<tr><td>N_NEG       </td> <td>4</td> <td>21-25</td></tr>
<tr><td>ICD10_R07.9 </td> <td>4</td> <td>21-25</td></tr>
</tbody>
</table>

With this tokenization, a query for "Patient" would return nothing. All tokens
have a prefix, so the token you search for must also have a prefix. If you wish
to match the text "Patient" exactly, you must search for "CS_Patient". If you
don't care how the word is cased or conjugated, search for "CI_patient".

Obviously, it isn't reasonable to expect an end-user of a search system to know
the these kinds of intricacies. There are two solutions to this problem.

1. Use a complex UI to allow the user to specify their intent, then generate the
   correct query for them
2. Use query-expansion to guess at their intent

A complex UI may be difficult to use and understand, but then users should have
a better understanding of what the system is doing, and it can be used to get
more accurate results if the user does understand it. Query-expansion allows you
to have a very simple UI (such as search bar to enter text), but doesn't
necessarily produce the best query for the user's intent, and can't be refined
necessarily, except by possibly opting-out of query expansion and specifying the
exact query syntax. Obviously, a combination of approaches can be used as well
to cater to different users and use cases.

<table class="basic">
<caption>Queries</caption>
<thead>
<tr>
<th>Attempted Query</th>
<th>Intent</th>
<th>Correct Query</th>
<th>Possible query-expansion</th>
<th>Comments</th>
</tr>
</thead>
<tbody>
<tr>
<td>Patients</td>
<td class="comment">Find the exact word "Patients"</td>
<td>CS_Patients</td>
<td>CI_patient</td>
<td class="comment">Query expansion would probably never guess exact searches</td>
</tr>
<tr>
<td>ALL</td>
<td class="comment">Find positive diagnoses of acute lymphoblastic leukemia</td>
<td>ICD10_C91.0 - N_NEG</td>
<td>CI_all</td>
<td class="comment">
Query expansion is unlikely to guess the diagnosis code 
since "all" is a common word
</td>
</tr>
<tr>
<td>C91.0</td>
<td class="comment">Find positive diagnoses of acute lymphoblastic leukemia</td>
<td>ICD10_C91.0 - N_NEG</td>
<td><span>ICD10_C91.0</span> <span>CI_c91.0</span></td>
<td class="comment">
The query-expansion could reasonably recognize the ICD-10 code when typed in.
However, it probably would not include the "- N_NEG",
since query-expansion can't know the user isn't searching for explicit
documentation of ruling out of ALL.
Query-expansion also can't rule out that the user may be looking
for that word in the text itself, so it can search for it too.
</td>
</tr>
</tbody>
</table>
