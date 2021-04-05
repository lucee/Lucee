component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-2374", function() {
			it( title='Checking url scope with multiple dot notation', body=function( currentSpec ) {
				if(Not structkeyexists(url,"res") && Not structkeyexists(url,"test")){
					cflocation(url="http://#cgi.HTTP_HOST##cgi.script_name#?method=runremote&ts=10&res...temp=1",addtoken="false");
				}
				expect(structCount(url)).toBe('2');
				expect(structkeyexists(url,"res")).toBe('False');
				expect(structkeyexists(url,"res...temp")).toBe('true');
			});

			it( title='Checking url scope with single dot notation', body=function( currentSpec ) {
				if(Not structkeyexists(url,"test")){
					cflocation(url="http://#cgi.HTTP_HOST##cgi.script_name#?method=runremote&mt=10&test.temp=lucee",addtoken="false");
				}
				expect(url.test.temp EQ "lucee").toBe('true');
				expect(structCount(url)).toBe('2');
				expect(structkeyexists(url,"test")).toBe('False');
				expect(structkeyexists(url,"test.temp")).toBe('true');
			});
		});
	}
} 
