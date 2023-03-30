component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" {
	function beforeAll(){
		variables.uri = createURI("LDEV3845");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-3845", function() {
			it(title = "Checking cfmail tag with a utf8 email address", skip=isNotAvailable(), body = function( currentSpec ) {
				local.subject = "test-LDEV3845-1";
				local.addr = "läs@lucee.org";
				local.result = _InternalRequest(
					template:"#variables.uri#/index.cfm",
					form: {
						email: addr,
						subject: subject,
						charset: "utf-8"
					}
				);
				expect( local.result.filecontent.trim() ).toBe( 'ok' );
				fetchMails( addr, subject );

			});

			it(title = "Checking cfmail tag with a non-utf8 email address", skip=isNotAvailable(), body = function( currentSpec ) {
				local.subject = "test-LDEV3845-2";
				local.addr = "las@lucee.org";
				local.result = _InternalRequest(
					template:"#variables.uri#/index.cfm",
					form: {
						email: addr,
						subject: subject,
						charset: "ISO-8859-1"
					}
				);
				expect( local.result.filecontent.trim() ).toBe( 'ok' );
				fetchMails( addr, subject );
			});
		});
	}

	private function fetchMails( required string username, subject="" ){
		var pop = server.getTestService("pop");
		pop action="getAll"
			name="local.emails"
			server="#pop.server#"
			username="#arguments.username#"
			password="#arguments.username#"
			port="#pop.PORT_INSECURE#"
			secure="no"; // greenmail creates a mailbox based on the email address, password is the to address

		if ( len( arguments.subject ) > 0) {
			// filter by subject
		}
		systemOutput( "--------------- [ #arguments.username#] had #emails.recordcount# emails", true );
		systemOutput( emails, true );
		for ( local.email in emails )
			systemOutput( email.header, true );
		systemOutput( "---------------", true );

		return emails;
	}

	private function isNotAvailable() {
		return structCount( server.getTestService( "smtp" ) ) == 0
			&& structCount( server.getTestService( "pop" ) ) == 0;
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
