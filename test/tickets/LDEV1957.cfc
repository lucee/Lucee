component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1957", body=function(){
			it(title="Checking SERVER.separator.line EQ newLine() on #SERVER.os.name#", body=function(){
				expect(SERVER.separator.line EQ newline()).toBeTRUE();
			});
		});
	}
}
