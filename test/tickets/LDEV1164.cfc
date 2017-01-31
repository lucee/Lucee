<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run(){
			describe( title="Test suite for LDEV-1164", body=function(){
				it(title="checking cfhttp call", body=function(){
					var result = httpCall();
					expect(result.errordetail).toBe('');
					expect(result.statuscode).toBe('200 OK');
				});
			});
		}
	</cfscript>

	<cffunction name="httpCall" access="private" returntype="any">
		<cfhttp url="https://www.peri.com/en" timeout="10" method="HEAD">
		<cfreturn cfhttp />
	</cffunction>
</cfcomponent>