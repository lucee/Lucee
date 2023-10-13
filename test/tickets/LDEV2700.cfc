component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){
		describe( "Testcase for an empty argument on LDEV2700 ", function(){
			it( "Checking for an empty argument on LDEV2700", function(){
				```
				<cffunction name="test" >
					<cfargument name="a" type="numeric" required="false">
					<cfset result = isDefined(arguments.a)>
					<cfreturn result>
				</cffunction>
				```
				expect(function(){
					test
				}).tothrow()
			});
		});
	}
}