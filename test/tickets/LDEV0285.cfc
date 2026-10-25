component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-285", function() {
			it(title="Check parsing, with coldfusion tag in commented out before 'cfscript' tag", body = function( currentSpec ) {
				// outside cfscript "//" is not a comment, so the tag is real
				var uri=createURI("LDEV0285/App1.cfc");
				expect(function(){
					_InternalRequest(
						template:uri
					);
				}).toThrow(regex="undefined tag \[cflucee\]");
			});

			it(title="Check parsing, with coldfusion tag in commented out inside 'cfscript' tag", body = function( currentSpec ) {
				var uri=createURI("LDEV0285/App2.cfc");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});

			it(title="Check parsing, with coldfusion tag in commented out in tag based component", body = function( currentSpec ) {
				// in a tag based component "//" is not a comment, so the tag is real
				var uri=createURI("LDEV0285/App3.cfc");
				expect(function(){
					_InternalRequest(
						template:uri
					);
				}).toThrow(regex="undefined tag \[cflucee\]");
			});

			it(title="Check parsing, with cfargument tag commented out in a script based component", body = function( currentSpec ) {
				var uri=createURI("LDEV0285/App4.cfc");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});

			it(title="Check parsing, with unclosed cfif in a line comment in a script based component", body = function( currentSpec ) {
				expect(new LDEV0285.LineComment().test()).toBe("line");
			});

			it(title="Check parsing, with unclosed cfif in a block comment in a script based component", body = function( currentSpec ) {
				expect(new LDEV0285.BlockComment().test()).toBe("block");
			});

			it(title="Check parsing, with unclosed cfif in a string in a script based component", body = function( currentSpec ) {
				expect(new LDEV0285.InString().test()).toBe("this is <cfif xys>");
			});

			it(title="Check parsing, broken tag based component still reports the tag error", body = function( currentSpec ) {
				expect(function(){
					new LDEV0285.BrokenTag();
				}).toThrow(regex="Start and End Tag do not match \[cfif-cfcomponent\]");
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
