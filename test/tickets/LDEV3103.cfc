component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV-3103", function() {
			it( title = "check refind", body = function( currentSpec ){

			    var startPos=20; 
				// this file should be LF

var text="[pegdown](https://github.com/sirthias/pegdown). Please see both the [official Markdown website](http://daringfireball.net/projects/markdown/) and the [pegdown repository](https://github.com/sirthias/pegdown) for the supported syntax.
#### Syntax highlighting:
<pre>
```
echo( x );
```
</pre>";

				var referenceRegex = "```([a-z\+]+)?\n(.*?)\n```"; 
 				var match = ReFind( referenceRegex, text, startPos, true ); 
				
				expect(len(match.len)).toBe(3);

				expect(match.len[1]).toBe(18);
				expect(match.len[2]).toBe(0);
				expect(match.len[3]).toBe(10);

				expect(match.pos[1]).toBe(265);
				expect(match.pos[2]).toBe(0);
				expect(match.pos[3]).toBe(269);

				expect(isNull(match.match[2])).toBe(true);
			});
		});
	}

}