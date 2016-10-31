<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
	function run( testResults , testBox ) {
		describe('include cfc', function(){
			beforeEach(function( currentSpec ){
				// runs before each spec in this suite group
				include template="LDEV0206/udfs.cfm";
				include template="LDEV0206/udfs.cfc";
			});

			it("Checking the existence of function, whose definition is in included cfm file", function(){
				expect(structKeyExists(variables, "getCountry")).toBeTrue();
			});

			it("Checking the existence of function, whose definition is in included cfc file", function(){
				expect(structKeyExists(variables, "getCountries")).toBeTrue();
			});
		});
	}
	</cfscript>
</cfcomponent>