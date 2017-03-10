<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-414", function() {
				it(title="checking http().addParam function, with type ='File' without file attribute ", body = function( currentSpec ) {
					try {
						variables.httpArgs = { method: "POST", url: "http://google.com" };
						variables.paramArgs = { type: "file", name: "file", value: "/whatever/path/doesntmatter.txt" };
						variables.connection = new http( argumentCollection: variables.httpArgs );
						variables.connection.addParam( argumentCollection: variables.paramArgs );
						variables.connection.send();
					} catch ( any e){
						var result = e.message;
						if(result!="attribute [file] is required for tag [httpparam] if type is [file]") 
							rethrow;
					}
					expect(result).toBe("attribute [file] is required for tag [httpparam] if type is [file]");
				});

				it(title="checking cfhttpparam in tag, with type = 'file' without file attribute", body = function( currentSpec ) {
					result = httpparamtag();
					expect(result).toBe("attribute [file] is required for tag [httpparam] if type is [file]");
				});
			});
		}
	</cfscript>

	<cffunction name="httpparamtag" access="private" returntype="Any">
		<cfset result = "">
		<cftry>
			<cfhttp method="post" url="http://google.com">
				<cfhttpparam type="file" name="file" value="file">
			</cfhttp>
			<cfcatch type="any">
				<cfset result = cfcatch.message>
			</cfcatch>
		</cftry>
		<cfreturn result>
	</cffunction>
</cfcomponent>
