component extends="org.lucee.cfml.test.LuceeTestCase" labels="markdown" {

    variables.markdownString=trim("
    #### Headline with ID ####

Another headline with ID
------------------------

* List with ID

Links: [Foo] (##headid)

This is ***TXTMARK***
This is ***TXTMARK***
_This Word is italic_

``
This is code!
``

1. First list item
   - First nested list item
     - Second nested list item


````This is code!````

``
`
This is code!
`
``

```
This is code!
```

> Dorothy followed her through many of the beautiful rooms in her castle.

![Lucee](https://docs.lucee.org/assets/images/lucee-logo.png)

I love supporting the **[EFF](https://eff.org)**.

This is the *[Markdown Guide](https://www.markdownguide.org)*.

See the section on [`code`](##code).
");


variables.htmlString=trim('<h2>Headline with ID</h2>
<h2>Another headline with ID</h2>
<ul>
<li>List with ID</li>
</ul>
<p>Links: [Foo] (##headid)</p>
<p>This is <em><strong>TXTMARK</strong></em>
This is <em><strong>TXTMARK</strong></em>
<em>This Word is italic</em></p>
<p><code>This is code!</code></p>
<ol>
<li>First list item
<ul>
<li>First nested list item
<ul>
<li>Second nested list item</li>
</ul>
</li>
</ul>
</li>
</ol>
<p><code>This is code!</code></p>
<p><code>` This is code! `</code></p>
<pre><code>This is code!
</code></pre>
<blockquote>
<p>Dorothy followed her through many of the beautiful rooms in her castle.</p>
</blockquote>
<p><img src="https://docs.lucee.org/assets/images/lucee-logo.png" alt="Lucee" /></p>
<p>I love supporting the <strong><a href="https://eff.org">EFF</a></strong>.</p>
<p>This is the <em><a href="https://www.markdownguide.org">Markdown Guide</a></em>.</p>
<p>See the section on <a href="##code"><code>code</code></a>.</p>
');
	variables.htmlString = forceLF( variables.htmlString );
	variables.markdownString = forceLF(variables.markdownString);

	function run( testResults, testBox ){
		describe( title="Testcase for markdownToHTML()", body=function() {
			it( title = "Checking with markdownToHTML with a string", body=function( currentSpec ) {
				expect(variables.htmlString.trim()).toBe( markdownToHTML(variables.markdownString).trim() );
			});

			it( title = "Checking with markdownToHTML with a file", body=function( currentSpec ) {
				var file=getTempFile( getTempDirectory(), "markdown", "md" );
				try {
					fileWrite(file, markdownString);
					expect(variables.htmlString.trim()).toBe( markdownToHTML(variables.markdownString).trim() );
				}
				finally {
					if( fileExists( file ) ) fileDelete( file );
				}
			});
		});
	} 

	private string function forceLF (required string str ){
		return replace( arguments.str, "#chr(13)##chr(10)#", chr(10), "all" );
	}

}