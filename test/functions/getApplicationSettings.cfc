component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults, testBox ) {
		describe("Testcase for getApplicationSettings()", function() {

			it( title="get unsupported application settings (this vars)", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template: createURI("unSupported.cfm" )
				);
				expect( result.filecontent.deserializeJSON() ).toBe( [ "nonStandardSetting", "useJavaAsRegexEngine"] );
			});

			it( title="getApplicationSettings(onlySupported=true)", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template: createURI("onlySupported.cfm" ),
					url: {
						onlySupported: true
					}
				);
				expect( result.filecontent.trim() ).toBeFalse();
			});

			it( title="getApplicationSettings(onlySupported=false)", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template: createURI("onlySupported.cfm" ),
					url: {
						onlySupported: false // default
					}
				);
				expect( result.filecontent.trim() ).toBeTrue();
			});

			it( title="getApplicationSettings()", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template: createURI("onlySupported.cfm" )
				);
				expect( result.filecontent.trim() ).toBeTrue();
			});

			it( title="getApplicationSettings(suppressFunction=true)", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template: createURI("suppressFunction.cfm" ),
					url: {
						suppressFunction: true
					}
				);
				expect( result.filecontent.trim() ).toBeFalse();
			});

			it( title="getApplicationSettings(suppressFunction=false)", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template: createURI("suppressFunction.cfm" ),
					url: {
						suppressFunction: false // default
					}
				);
				expect( result.filecontent.trim() ).toBeTrue();
			});

			it( title="getApplicationSettings()", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template: createURI("suppressFunction.cfm" )
				);
				expect( result.filecontent.trim() ).toBeTrue();
			});

			it( title="getApplicationSettings()", body=function( currentSpec ) {
				var as = getApplicationSettings(onlySupported=true);
				expect( as ).toHaveKey("bufferoutput");
				expect( as ).toHaveKey("suppresscontent");
				expect( as ).toHaveKey("componentDataMemberAccess");
				expect( as.componentDataMemberAccess ).toBe( "public" );
				expect( as ).toHaveKey("inspectTemplate");
				expect( as.inspectTemplate ).toBe( "once" );
			});
		});
	}

	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/";
		return baseURI & "getApplicationSettings/" & calledName;
	}

}