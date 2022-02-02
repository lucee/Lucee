component extends="org.lucee.cfml.test.LuceeTestCase" {
	
    variables.markdownString=trim(" 
    #### Headline with ID ####

Another headline with ID
------------------------

* List with ID 

Links: [Foo] (##headid)

This is ***TXTMARK***
This is ***TXTMARK***
    
``
This is code!
``

1. First list item
   - First nested list item
     - Second nested list item
");

variables.htmlString=trim('<h2>Headline with ID</h2>
<h2>Another headline with ID</h2>
<ul>
<li>List with ID</li>
</ul>
<p>Links: <a href="##headid">Foo</a></p>
<p>This is <strong><em>TXTMARK</em></strong>
This is <strong><em>TXTMARK</em></strong></p>
<p><code>
This is code!
</code></p>
<ol>
<li>First list item</li>
<li>First nested list item<ul>
<li>Second nested list item</li>
</ul>
</li>
</ol>');
	
	function run( testResults, testBox ){
		describe( title="Testcase for markdownToHTML()", body=function() {
			it( title = "Checking with markdownToHTML with a string", body=function( currentSpec ) {
				assertEquals(
					variables.htmlString,
					markdownToHTML(variables.markdownString)
				);
			});

			it( title = "Checking with markdownToHTML with a file", body=function( currentSpec ) {
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
				var file=curr&"markdownToHTML/test.md";
				try {
					fileWrite(file, markdownString);
					assertEquals(
						variables.htmlString,
						markdownToHTML(file)
					);
				}
				finally {
					if(fileExists(file)) fileDelete(file);
				}
			});
		});
	}
}