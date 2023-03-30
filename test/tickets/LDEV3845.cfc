component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" {
	function beforeAll(){
		variables.uri = createURI("LDEV3845");
		fetchMails(); // clear out the mailbox
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-3845", function() {
			it(title = "Checking cfmail tag with a utf8 email address", skip=isAvailable(), body = function( currentSpec ) {
				local.subject = "test-LDEV3845-1";
				local.result = _InternalRequest(
					template:"#variables.uri#/index.cfm",
					form: {
						email: "läs@lucee.org",
						subject: subject
					}
				);
				expect(local.result.filecontent.trim()).toBe('ok');
				fetchMails( subject );

			});

			it(title = "Checking cfmail tag with a non-utf8 email address", skip=isAvailable(), body = function( currentSpec ) {
				local.subject = "test-LDEV3845-2";
				local.result = _InternalRequest(
					template:"#variables.uri#/index.cfm",
					form: {
						email: "las@lucee.org",
						subject: subject
					}
				);
				expect(local.result.filecontent.trim()).toBe('ok');
				fetchMails( subject );
			});
		});
	}

	private function fetchMails( subject="" ){
		var pop = server.getTestService("pop");
		pop action="getAll"
			name="local.emails"
			server="#pop.server#"
			password="#pop.password#"
			port="#pop.PORT_INSECURE#"
			secure="no"
			username="luceeldev3545pop@localhost";

		if ( len( arguments.subject ) > 0) {
			// filter by subject
		}
		systemOutput( "---------------", true );
		systemOutput( emails, true );
		systemOutput( "---------------", true );

		return emails;
	}

	private function isAvailable() {
		return structCount( server.getTestService( "smtp" ) ) > 0
			&& structCount( server.getTestService( "pop" ) ) > 0;
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
