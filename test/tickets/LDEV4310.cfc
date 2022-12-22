component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4310 - getApplicationSetting().regex", function() {

			it( title="regex_type_java", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : contractPath("LDEV4310/regex_type_java/index.cfm" )
				);
				expect( result.filecontent.trim() ).toBe( '{"TYPE":"java"}' );
			});

			it( title="regex_type_perl", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : contractPath("LDEV4310/regex_type_perl/index.cfm" )
				);
				expect( result.filecontent.trim() ).toBe( '{"TYPE":"perl"}' );
			});

			it( title="useJavaAsRegexEngine_false check useJavaAsRegexEngine", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : contractPath("LDEV4310/useJavaAsRegexEngine_false/index.cfm" ),
					url: {
						check: "useJavaAsRegexEngine"
					}
				);
				expect( result.filecontent.trim() ).toBe( serializeJSON("false") ); 
			});

			it( title="useJavaAsRegexEngine_true, check useJavaAsRegexEngine", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : contractPath("LDEV4310/useJavaAsRegexEngine_true/index.cfm" ),
					url: { 
						check: "useJavaAsRegexEngine"
					}
				);
				expect( result.filecontent.trim() ).toBe( serializeJSON("true") ); 
			});

			it( title="useJavaAsRegexEngine_false, check regex", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : contractPath("LDEV4310/useJavaAsRegexEngine_false/index.cfm" ),
					url: { 
						check: "regex"
					}
				);
				expect( result.filecontent.trim() ).toBe( '{"TYPE":"perl"}' ); 
			});

			it( title="useJavaAsRegexEngine_true, check regex", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : contractPath("LDEV4310/useJavaAsRegexEngine_true/index.cfm" ),
					url: { 
						check: "regex"
					}
				);
				expect( result.filecontent.trim() ).toBe( '{"TYPE":"java"}' );
			});

		});
	}

}
