component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function run( testResults, testBox ) {
		describe( "LDEV-6085: admin scope cascading setting should persist after save", function() {

			it( title="Admin scope cascading setting saves correctly", body=function( currentSpec ) {

				var adminPassword = request.SERVERADMINPASSWORD ?: "";

				// Get current setting
				admin action="getScope"
					type="server"
					password=adminPassword
					returnVariable="scopeBefore";

				var originalType = scopeBefore.scopeCascadingType;
				systemOutput( "Original scope cascading: #originalType#", true );

				// Change to a different setting
				var newType = ( originalType == "standard" ) ? "strict" : "standard";
				systemOutput( "Changing to: #newType#", true );

				// Update the setting
				admin action="updateScope"
					type="server"
					password=adminPassword
					scopeCascadingType=newType
					sessionType=scopeBefore.sessionType
					localMode=scopeBefore.localMode
					allowImplicidQueryCall=scopeBefore.allowImplicidQueryCall
					mergeFormAndUrl=scopeBefore.mergeFormAndUrl
					formUrlAsStruct=scopeBefore.formUrlAsStruct
					clientTimeout=scopeBefore.clientTimeout
					sessionTimeout=scopeBefore.sessionTimeout
					applicationTimeout=scopeBefore.applicationTimeout
					sessionManagement=scopeBefore.sessionManagement
					clientManagement=scopeBefore.clientManagement
					clientCookies=scopeBefore.clientCookies
					domaincookies=scopeBefore.domaincookies
					sessionStorage=scopeBefore.sessionStorage
					clientStorage=scopeBefore.clientStorage
					cgiReadonly=scopeBefore.cgiReadonly;

				// Read it back - this should show the new value
				admin action="getScope"
					type="server"
					password=adminPassword
					returnVariable="scopeAfter";

				systemOutput( "After update: #scopeAfter.scopeCascadingType#", true );

				// Verify the change persisted
				expect( scopeAfter.scopeCascadingType ).toBe( newType, "Scope cascading type should be updated to #newType#" );

				// Clean up - restore original setting
				admin action="updateScope"
					type="server"
					password=adminPassword
					scopeCascadingType=originalType
					sessionType=scopeBefore.sessionType
					localMode=scopeBefore.localMode
					allowImplicidQueryCall=scopeBefore.allowImplicidQueryCall
					mergeFormAndUrl=scopeBefore.mergeFormAndUrl
					formUrlAsStruct=scopeBefore.formUrlAsStruct
					clientTimeout=scopeBefore.clientTimeout
					sessionTimeout=scopeBefore.sessionTimeout
					applicationTimeout=scopeBefore.applicationTimeout
					sessionManagement=scopeBefore.sessionManagement
					clientManagement=scopeBefore.clientManagement
					clientCookies=scopeBefore.clientCookies
					domaincookies=scopeBefore.domaincookies
					sessionStorage=scopeBefore.sessionStorage
					clientStorage=scopeBefore.clientStorage
					cgiReadonly=scopeBefore.cgiReadonly;
			});

		});

		describe( "Template charset in compiler settings should persist after save", function() {

			it( title="Admin compiler settings templateCharset saves correctly", body=function( currentSpec ) {

				var adminPassword = request.SERVERADMINPASSWORD ?: "";

				// Get current compiler settings
				admin action="getCompilerSettings"
					type="server"
					password=adminPassword
					returnVariable="settingsBefore";

				var originalCharset = settingsBefore.templateCharset.name();
				systemOutput( "Original template charset: #originalCharset#", true );

				// Change to a different charset
				var newCharset = ( originalCharset == "UTF-8" ) ? "ISO-8859-1" : "UTF-8";
				systemOutput( "Changing to: #newCharset#", true );

				// Update the compiler settings including templateCharset
				admin action="updateCompilerSettings"
					type="server"
					password=adminPassword
					dotNotationUpperCase=settingsBefore.DotNotationUpperCase
					suppressWSBeforeArg=settingsBefore.suppressWSBeforeArg
					nullSupport=settingsBefore.nullSupport
					handleUnquotedAttrValueAsString=settingsBefore.handleUnquotedAttrValueAsString
					externalizeStringGTE=settingsBefore.externalizeStringGTE
					preciseMath=settingsBefore.preciseMath
					templateCharset=newCharset;

				// Read it back - this should show the new value
				admin action="getCompilerSettings"
					type="server"
					password=adminPassword
					returnVariable="settingsAfter";

				var actualCharset = settingsAfter.templateCharset.name();
				systemOutput( "After update: #actualCharset#", true );

				// Verify the change persisted
				expect( actualCharset ).toBe( newCharset, "Template charset should be updated to #newCharset#" );

				// Clean up - restore original setting
				admin action="updateCompilerSettings"
					type="server"
					password=adminPassword
					dotNotationUpperCase=settingsBefore.DotNotationUpperCase
					suppressWSBeforeArg=settingsBefore.suppressWSBeforeArg
					nullSupport=settingsBefore.nullSupport
					handleUnquotedAttrValueAsString=settingsBefore.handleUnquotedAttrValueAsString
					externalizeStringGTE=settingsBefore.externalizeStringGTE
					preciseMath=settingsBefore.preciseMath
					templateCharset=originalCharset;
			});

		});
	}

}
