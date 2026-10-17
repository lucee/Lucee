component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI( "LDEV4122" );
	}
	function run( testResults , testBox ) {
		describe(title="Testcase for LDEV-4122", body=function() {
			it(title="checking UDF has multiple arguments with same name", body=function( currentSpec ) {
				expect( function() {
					_InternalRequest( template:"#variables.uri#/LDEV4122.cfm" );
				}).toThrow( regex="more than once" );
			});
			it(title="checking cffunction has multiple cfargument with same name (case insensitive)", body=function( currentSpec ) {
				expect( function() {
					_InternalRequest( template:"#variables.uri#/tag.cfm" );
				}).toThrow( regex="more than once" );
			});
			it(title="checking closure has multiple arguments with same name", body=function( currentSpec ) {
				expect( function() {
					_InternalRequest( template:"#variables.uri#/closure.cfm" );
				}).toThrow( regex="more than once" );
			});
			it(title="different argument names are fine", body=function( currentSpec ) {
				var result = _InternalRequest( template:"#variables.uri#/different.cfm" ).filecontent;
				expect( trim( result ) ).toBe( "3" );
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
