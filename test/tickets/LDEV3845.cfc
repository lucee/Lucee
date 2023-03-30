component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" {
	function beforeAll(){
		variables.uri = createURI("LDEV3845");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-3845", function() {

			it(title = "Checking cfmail tag with a non-utf8 email address", skip=isNotAvailable(), body = function( currentSpec ) {
				local.subject = "test-LDEV3845-non-utf8";
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
				local.mails= fetchMails( addr, subject );
				expect( mails.recordcount ).toBe( 1 );

				local.to = getHeaderFromMail( mails, "to" );
				expect( to ).toInclude( addr );
				//expect( to ).toInclude( "=?utf-8?" );
			});

			it(title = "Checking cfmail tag with a utf8 email address", skip=isNotAvailable(), body = function( currentSpec ) {
				local.subject = "test-LDEV3845-utf8";
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
				local.mails = fetchMails( addr, subject );
				expect( mails.recordcount ).toBe( 1 );
				
				local.to = getHeaderFromMail( mails, "to" );
				expect( to ).toInclude( addr );
				expect( to ).toInclude( "=?utf-8?" );
			});

		});
	}

	private query function fetchMails( required string username, subject="" ){
		var pop = server.getTestService("pop");
		pop action="getAll"
			name="local.emails"
			server="#pop.server#"
			username="#arguments.username#"
			password="#arguments.username#"
			port="#pop.PORT_INSECURE#"
			secure="no"; // greenmail creates a mailbox based on the email address, password is the to address

		/*
		if ( len( arguments.subject ) > 0) {
			// TODO filter by subject
		}

		systemOutput( "--------------- [ #arguments.username#] had #emails.recordcount# emails", true );
		systemOutput( emails, true );
		for ( local.email in emails )
			systemOutput( email.header, true );
		systemOutput( "---------------", true );
		*/
		return emails;
	}

	private string function getHeaderFromMail( mails, header ){
		var headers = listToArray( arguments.mails.header[ 1 ] , chr( 10 ) );
		loop array=#headers# item="local.h" {
			if ( listFirst( h, ":" ) eq arguments.header )
				return trim( listRest( h, ":" ) );
		}
		systemOutput( headers, true );
		return "";
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
