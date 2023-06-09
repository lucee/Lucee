component extends = "org.lucee.cfml.test.LuceeTestCase" lables="static" skip="false" {

	
	function beforeAll(){
		fileWrite("./LDEV4469/Base.cfc", 'component { include "include.cfm";}');
	};

	function afterAll(){
		fileWrite("./LDEV4469/Base.cfc", 'component { include "include.cfm";}');
	};
	
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-4469 static final", function() {

			it(title="checking changed include file doesn't break with final static", body=function( currentSpec ) {

				local.result = _InternalRequest(
					template: createURI("index.cfm" )
				);
				expect( result.filecontent.trim() ).toBe("ok");

				fileWrite("./LDEV4469/Base.cfc", 'component { include "include2.cfm";}');

				local.result = _InternalRequest(
					template: createURI("index.cfm" )
				);
				expect( result.filecontent.trim() ).toBe("ok");
			});
			
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/";
		return baseURI & "LDEV4469/" & calledName;
	}

}