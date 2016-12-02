<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		variables.isLucee5 = false;
		if( structKeyExists(server, "lucee") && listFirst(server.lucee.version, ".") == "5" )
			variables.isLucee5 = true;
		function isNotSupported(s1) {
			return s1;
		}

		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-910", function() {
				it(title="checking cfpop tag, with 'secure' attribute in lucee 5", skip=isNotSupported(!variables.isLucee5), body = function( currentSpec ) {
					var uri=createURI("LDEV0910/test.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.filecontent.trim()).toBe("A SSL/TLS-connection is required for authentication.");
				});

				it(title="checking cfpop tag, without 'secure' attribute", body = function( currentSpec ) {
					var result = cfpopWithoutSecureAttr();
					expect(result).toBe("A SSL/TLS-connection is required for authentication.");
				});
			});
		}

		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>

	<cffunction name="cfpopWithoutSecureAttr" access="private" returntype="string">
		<cftry>
			<cfpop server = "127.0.0.1" username = "test1@mail.local" password="password" action ="getHeaderOnly" name ="mailContent" timeout="60"> 
			<cfcatch type = "any">
				<cfset mailContent = cfcatch.message>
			</cfcatch> 
		</cftry>
		<cfreturn mailContent>
	</cffunction>
</cfcomponent>
