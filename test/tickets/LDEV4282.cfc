component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV4282";
		variables.uri = createURI("LDEV4282");
		variables.target = dir & "/test.cfm";
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4282 / LDEV-4286", function() {
			it(title="check changed file compiles after initial bytcode error", body=function( currentSpec ){
				
				if ( !directoryExists( variables.dir ) )
					directoryCreate( variables.dir );
				if ( fileExists( variables.target ) )
					fileDelete( variables.target );

				fileWrite( variables.target ,
				"<cfscript>test(1,2,3);
					function test(a,b,c){
						function(){
						}
					}</cfscript>"
				); // bytecode error LDEV-4286
				
				expect(function(){
					_internalRequest(
						template = "#variables.uri#/test.cfm"
					);
				}).toThrow();

				fileWrite( variables.target ,"<cfscript>echo('ok');</cfscript>");
				var res = {
					filecontent:"not ok"
				};
				expect(function(){
					res = _internalRequest(
						template = "#variables.uri#/test.cfm"
					);
				}).NotToThrow();
				expect( res.fileContent.trim() ).toBe( "ok" );
			});
			
		});
	}

	function afterAll() {
		if ( fileExists( variables.target ) )
			fileDelete("#variables.dir#/test.cfm");
		if ( directoryExists( variables.dir ) )
			directoryDelete( variables.dir );
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
