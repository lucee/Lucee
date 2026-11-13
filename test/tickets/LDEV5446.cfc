component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	/*
		LDEV-5446: Cancel on Create AI Connection used to be a submit button named mainAction,
		so the browser posted the form instead of leaving the page. Cancel has to be a button
		that navigates back to the AI list, and that return must not create a connection.
	*/

	function run( testResults, testBox ) {
		describe( "LDEV-5446 admin create AI connection cancel", function() {

			it( title="Cancel returns to the AI list without submitting or creating a connection", body=function( currentSpec ) {
				var adminRoot = getAdminRoot();
				expect( len( adminRoot ) ).toBeGT( 0, "the admin is not available to this test" );

				var name = "ldev5446-" & lCase( replace( createUUID(), "-", "", "all" ) );
				expect( arrayFindNoCase( aiNames(), name ) ).toBe( 0 );

				try {
					var login = _internalRequest(
						template: adminRoot & "index.cfm",
						forms: { login_passwordserver: server.SERVERADMINPASSWORD, lang: "en", rememberMe: "s", submit: "submit" }
					);
					expect( login.status ).toBe( 200 );
					var cookies = { cfid: login.session.cfid, cftoken: login.session.cftoken };

					var before = serializeJSON( aiNames() );
					var createPage = _internalRequest(
						template: adminRoot & "index.cfm",
						urls: { action: "services.ai", action2: "create" },
						forms: { class: "lucee.runtime.ai.openai.OpenAIEngine", run: "create" },
						cookies: cookies
					);
					expect( createPage.status ).toBe( 200, left( createPage.fileContent ?: "", 500 ) );
					expect( serializeJSON( aiNames() ) ).toBe( before, "opening the create form created an AI connection" );

					var formHtml = createForm( createPage.fileContent );
					var cancel = cancelControl( formHtml );
					expect( cancel.type ).toBe( "button", cancel.tag );
					expect( cancel.name ).toBe( "cancel" );
					expect( cancel.value ).toBe( "cancel" );
					expect( cancel ).notToHaveKey( "formaction" );
					expect( lCase( cancel.onclick ?: "" ) ).notToInclude( "submit" );

					var submits = submitControls( formHtml );
					expect( arrayLen( submits ) ).toBe( 1, "the create form has more than one submit control" );
					expect( submits[ 1 ].name ).toBe( "mainAction" );
					expect( submits[ 1 ].value ).toBe( "submit" );

					var location = cancelLocation( cancel.onclick );
					expect( location ).toInclude( "action=services.ai" );
					expect( location ).notToInclude( "action2" );
					expect( lCase( location ) ).notToInclude( "mainaction" );

					var returned = _internalRequest(
						template: listFirst( cancelTarget( adminRoot, location ), "?" ),
						urls: queryUrls( cancelTarget( adminRoot, location ) ),
						cookies: cookies
					);
					expect( returned.status ).toBe( 200, left( returned.fileContent ?: "", 500 ) );
					expect( returned.fileContent ).toInclude( "Create a new AI connection" );
					expect( returned.fileContent ).notToInclude( 'name="custom_secretKey"' );
					expect( serializeJSON( aiNames() ) ).toBe( before, "returning from Cancel created an AI connection" );

					// the old Cancel control posted mainAction=cancel; that post must not store a connection
					var posted = _internalRequest(
						template: adminRoot & "index.cfm",
						urls: { action: "services.ai", action2: "create" },
						forms: {
							mainAction: "cancel",
							class: "lucee.runtime.ai.openai.OpenAIEngine",
							name: name,
							"custom_secretKey": "local-only",
							"custom_message": "local-only",
							"custom_type": "other"
						},
						cookies: cookies
					);
					expect( listFind( "200,302", posted.status ) ).toBeGT( 0, "cancel post status #posted.status#" );
					expect( arrayFindNoCase( aiNames(), name ) ).toBe( 0 );
					expect( serializeJSON( aiNames() ) ).toBe( before, "a cancel post created or changed an AI connection" );
				}
				finally {
					if ( arrayFindNoCase( aiNames(), name ) ) {
						admin action="removeAIConnection" type="server" password=server.SERVERADMINPASSWORD name=name;
					}
				}
			});

		});
	}

	private string function getAdminRoot() {
		var adminRoot = server.system.environment.LUCEE_TEST_ADMIN_PATH ?: "";
		if ( len( adminRoot ) ) return adminRoot;
		for ( adminRoot in [ "/admin/", "/lucee/admin/" ] ) {
			if ( !isNull( getPageContext().getRelativePageSourceExisting( adminRoot & "index.cfm" ) ) ) return adminRoot;
		}
		return "";
	}

	private array function aiNames() {
		admin action="getAIConnections" type="server" password=server.SERVERADMINPASSWORD returnVariable="local.connections";
		var names = [];
		loop query=local.connections {
			arrayAppend( names, local.connections.name );
		}
		return names;
	}

	private string function createForm( required string html ) {
		var forms = reMatch( "(?is)<form\b.*?</form>", arguments.html );
		for ( var formHtml in forms ) {
			if ( findNoCase( 'name="custom_secretKey"', formHtml ) ) return formHtml;
		}
		expect( false ).toBeTrue( "create form was not rendered: " & left( arguments.html, 500 ) );
		return "";
	}

	private struct function cancelControl( required string formHtml ) {
		var tags = reMatch( "(?is)<input\b[^>]*>", arguments.formHtml );
		for ( var tag in tags ) {
			var attrs = inputAttributes( tag );
			if ( ( attrs.name ?: "" ) == "cancel" ) {
				attrs.tag = tag;
				return attrs;
			}
		}
		expect( false ).toBeTrue( "Cancel control was not rendered: " & left( arguments.formHtml, 500 ) );
		return {};
	}

	private array function submitControls( required string formHtml ) {
		var found = [];
		var tags = reMatch( "(?is)<input\b[^>]*>", arguments.formHtml );
		for ( var tag in tags ) {
			var attrs = inputAttributes( tag );
			if ( ( attrs.type ?: "" ) == "submit" ) arrayAppend( found, attrs );
		}
		return found;
	}

	private struct function inputAttributes( required string tag ) {
		var attrs = {};
		var pairs = reMatch( "(?i)[a-z_:][-a-z0-9_:.]*=(?:""[^""]*""|'[^']*')", arguments.tag );
		for ( var pair in pairs ) {
			var key = lCase( listFirst( pair, "=" ) );
			var value = mid( pair, len( key ) + 2, len( pair ) );
			attrs[ key ] = mid( value, 2, len( value ) - 2 );
		}
		return attrs;
	}

	private string function cancelLocation( required string onclick ) {
		var found = reFind( "window\.location\s*=\s*'([^']*)'", arguments.onclick, 1, true );
		expect( arrayLen( found.pos ) ).toBeGTE( 2, arguments.onclick );
		expect( found.pos[ 2 ] ).toBeGT( 0, arguments.onclick );
		return mid( arguments.onclick, found.pos[ 2 ], found.len[ 2 ] );
	}

	private string function cancelTarget( required string adminRoot, required string location ) {
		if ( left( arguments.location, 1 ) == "/" || find( "://", arguments.location ) ) return arguments.location;
		return arguments.adminRoot & arguments.location;
	}

	private struct function queryUrls( required string target ) {
		var urls = {};
		if ( !find( "?", arguments.target ) ) return urls;
		for ( var pair in listToArray( listRest( arguments.target, "?" ), "&" ) ) {
			urls[ listFirst( pair, "=" ) ] = urlDecode( listRest( pair, "=" ) );
		}
		return urls;
	}

}
