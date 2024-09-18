component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {
		describe( title='LDEV-5092', body=function(){
			it( title='trigger java.util.ConcurrentModificationException', body=function() {
				var a = [];
				ArraySet(a,1,1000,"");
				ArrayEach(a, testJava, true);
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