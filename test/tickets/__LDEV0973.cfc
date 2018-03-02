component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test Case for LDEV-0973", function() {
			it( title='Checking URLEncode in cfhttp', body=function( currentSpec ) {
				cfhttp(url= "http://" &CGI.server_name &GetDirectoryFromPath( CGI.script_name ) &"LDEV0973/test.cfm?filtername=cold+fusion+lucee") { }
				var result = cfhttp.filecontent;
				expect(result).toBe('cold fusion lucee');
			});
		});
	}
}

