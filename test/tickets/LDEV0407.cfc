<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-407", function() {
				it(title="checking http() function with timeout attribute on request", body = function( currentSpec ) {
					try {
						var httpResponse = new http()
						.setUrl("http://httpbin.org/delay/3")
						.setTimeout(1)
						.setThrowOnError(true)
						.send()
						.getPrefix();
					} catch ( any e){
						var httpResponse = e.message;
					}
					expect(httpResponse).toBe("408 Request Time-out");
				});

				it(title="checking cfhttp tag with timeout attribute on request", body = function( currentSpec ) {
					var httpResponse = cfhttptag();
					expect(httpResponse).toBe("408 Request Time-out");
				});
			});
		}
	</cfscript>
	
	<cffunction name="cfhttptag" access="private" returntype="Any">
		<cfset result = "">
		<cftry>
			<cfhttp url="http://httpbin.org/delay/3" throwonerror="true" timeout="1">
			</cfhttp>
			<cfcatch type="any">
				<cfset result = cfcatch.message>
			</cfcatch>
		</cftry>
		<cfreturn result>
	</cffunction>
</cfcomponent>
