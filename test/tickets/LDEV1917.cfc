component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {
	function beforeAll(){
		variables.uri = createURI("LDEV1917");
	}
	function run( testResults , testBox ) {
		if(!hasCredentials()) return;
		describe( "test suite for LDEV-1917", function() {
			it(title = "cfprocparam passes null instead of empty strings with NVARCHAR cfsqltype", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					form: {
						datatype: "nvarchar"
					}
				);
				expect(local.result.filecontent.trim()).toBeTrue();
			});

			it(title = "cfprocparam passes null instead of empty strings with CHAR cfsqltype", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					form: {
						datatype: "char"
					}
				);
				expect(local.result.filecontent.trim()).toBeTrue();
			});
		});

		describe( "test suite for LDEV-4645", function() {

			it(title = "cfprocparam passes null instead of empty strings with NVARCHAR cfsqltype, col not null", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					form: {
						datatype: "nvarchar",
						notNull: true
					}
				);
				expect(local.result.filecontent.trim()).toBeTrue();
			});

			it(title = "cfprocparam passes null instead of empty strings with CHAR cfsqltype, col not null", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					form: {
						datatype: "char",
						notNull: true
					}
				);
				expect(local.result.filecontent.trim()).toBeTrue();
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function hasCredentials() {
		return (structCount(server.getDatasource("mysql")) gt 0);
	}
}