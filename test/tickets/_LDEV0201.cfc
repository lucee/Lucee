<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Test Suite for LDEV-201", function() {
				it(title = "Checking cfswitch case with boolean value in tag based", body = function( currentSpec ) {
					expect(tagBasedCfswitchcase()).toBe(1);
				});

				it(title = "Checking cfswitch case with boolean value in script based", body = function( currentSpec ) {
					var test = true;
					switch(true){
						case 1:
							var result = 1;
							break;
						case 0:
							var result = 0;
							break;
						default:
							var result = "defaultCase";
					}
					expect(result).toBe(1);
				});
			});
		}
	</cfscript>
	<cffunction name="tagBasedCfswitchcase">
		<cfset test = true>
		<cfswitch expression="#test#">
			<cfcase value="1">
				<cfset result = 1>
				<cfoutput>#result#</cfoutput>
			</cfcase>
			<cfcase value="0">
				<cfset result = 0>
				<cfoutput>#result#</cfoutput>
			</cfcase>
			<cfdefaultcase>
				<cfset result = "defaultcase">
				<cfoutput>#result#</cfoutput>
			</cfdefaultcase>
		</cfswitch>
		<cfreturn result>
	</cffunction>
</cfcomponent>