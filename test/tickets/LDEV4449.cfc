component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {
	
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-4449 numbers", function() {

			it(title="checking with preciseMath=false", body=function( currentSpec ) {

				local.result = _InternalRequest(
					template: createURI("index.cfm" ),
					url: {
						preciseMath: false // default
					}
				);
				expect( result.filecontent.trim() ).toBe("ok");
			});
			it(title="checking with preciseMath=true", body=function( currentSpec ) {

				local.result = _InternalRequest(
					template: createURI("index.cfm" ),
					url: {
						preciseMath: true // default
					}
				);
				expect( result.filecontent.trim() ).toBe("ok");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/";
		return baseURI & "LDEV4449/" & calledName;
	}

}