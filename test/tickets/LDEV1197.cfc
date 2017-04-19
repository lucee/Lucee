component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.serveradmin = "password";
		variables.Webadmin = "password";
		variables.uri = createURI("LDEV1197");
		createMapping();
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1197", function() {
			describe( "checking server Mapping", function() {
				it( title='checking server mapping with cfimport tag', body=function( currentSpec ) {
					var result = _InternalRequest(
						template:"#variables.uri#/server/import.cfm"
					);
					expect(result.filecontent.trim()).toBe('false');
				});

				it( title='checking server mapping with create object', body=function( currentSpec ) {
					var result = _InternalRequest(
						template:"#variables.uri#/server/object.cfm"
					);
					expect(result.filecontent.trim()).toBe('true');
				});
			});

			describe( "checking web Mapping", function(){
				it( title='checking web Mapping with cfimport tag', body=function( currentSpec ) {
					var result = _InternalRequest(
						template:"#variables.uri#/web/import.cfm"
					);
					expect(result.filecontent.trim()).toBe('false');
				});

				it( title='checking web mapping with create object', body=function( currentSpec ) {
					var result = _InternalRequest(
						template:"#variables.uri#/web/object.cfm"
					);
					expect(result.filecontent.trim()).toBe('true');
				});
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private string function createMapping(string calledName){
		 admin
			action="updateMapping"
			type="web"
			password="#variables.Webadmin#"

			virtual="/w1197"
			physical="#expandPath(variables.uri)#\"
			archive=""
			primary="physical"
			inspect=""
			toplevel="yes"
			remoteClients="";

		admin
			action="updateMapping"
			type="server"
			password="#variables.serveradmin#"

			virtual="/s1197"
			physical="#expandPath(variables.uri)#\"
			archive=""
			primary="physical"
			inspect=""
			toplevel="yes"
			remoteClients="";

	}
}