component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title='LDEV-5092', body=function(){
			it( title='trigger the NPE', body=function() {
				var a = [];
				ArraySet(a,1,1000,"");
				ArrayEach(a, testJava, true);
			});

			it( title='trigger java.util.ConcurrentModificationException', body=function() {
				
				var threadIds=[];
				var prefix="t5092x";
				loop from=1 to=5000 index="local.i" {
					var threadId=prefix&i;
					arrayAppend(threadIds, threadId);
					thread name=threadId {
						lsparseDateTime("2021.12.12 19:00");
					}
    
				}

				// wait for it
				try {
					thread action="join" name=threadIds.toList();
				}
				catch(e) {
					sleep(1000);
				}
				
				// simply rethrow the exception if one did occur in any thread (no interpretation needed)
				loop struct=cfthread index="local.k" item="local.val" {
					if(left(k,len(prefix))==prefix && structKeyExists(val, "ERROR")) throw val.error;
				}
			});
		});
	}

	private function testJava(){
		var oRegExMatcher = createObject("java", "java.util.regex.Matcher");
		var re = oRegExMatcher.quoteReplacement( "(?i)<!-- $$blah$$ -->" ); // NPE
		var re2 = oRegExMatcher.quoteReplacement( javacast("string", "1") );
		var sReturn = "string";
		sReturn.replaceAll(
			oRegExMatcher.quoteReplacement( "(?i)<!-- $$blah$$ -->" ),
			oRegExMatcher.quoteReplacement( javacast("string", "1") )
		);
		sReturn.replaceAll( re, re2 );
		return sReturn;
	}
}