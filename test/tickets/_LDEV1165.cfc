<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq">
	<cfscript>
		function beforeAll(){
			variables.query = queryNew("Name", "CF_SQL_VARCHAR");
			for (i = 0; i < 10; i++)
			{
				queryAddRow(query);
				querySetCell(query, "Name", "test_#i#");
			}
		}

		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-1165", function() {
				describe( "checking Query of Query with LIKE operator", function() {
					it(title="Checking QoQ, Using LIKE and Escape operator without the cfqueryparam", body = function( currentSpec ) {
						var result = LikeOperatorWithoutQueryparam();
						expect(result).toBe(10);
					});

					it(title="Checking QoQ, Using LIKE and Escape operator with the cfqueryparam", body = function( currentSpec ) {
						var result = LikeOperatorWithQueryparam();
						expect(result).toBe(10);
					});
				});

				describe( "checking Query of Query with NOT LIKE operator", function() {
					it(title="Checking QoQ, Using NOT LIKE and Escape operator without the cfqueryparam", body = function( currentSpec ) {
						var result = notLikeOperatorWithoutQueryparam();
						expect(result).toBe(0);
					});

					it(title="Checking QoQ, Using NOT LIKE and Escape operator with the cfqueryparam", body = function( currentSpec ) {
						var result = notLikeOperatorWithQueryparam();
						expect(result).toBe(0);
					});
				});
			});
		}
	</cfscript>
	
	<cffunction name="LikeOperatorWithoutQueryparam" access="private" returntype="Any">
		<cftry >
			<cfquery name="tmpqry" dbType="query">
				SELECT *
				FROM variables.query
				WHERE  Name LIKE 'test\__'  ESCAPE '\'
		 		ORDER BY Name
			</cfquery>
			<cfset qryResult = tmpqry.recordcount>
		<cfcatch>
			<cfset qryResult = cfcatch.message>
		</cfcatch>
		</cftry>
		<cfreturn qryResult>
	</cffunction>

	<cffunction name="LikeOperatorWithQueryparam" access="private" returntype="Any">
		<cftry >
			<cfquery name="tmpqry" dbType="query">
				SELECT *
				FROM variables.query
				WHERE  Name LIKE <cfqueryparam value="test\__" cfsqltype="CF_SQL_VARCHAR"> ESCAPE '\'
		 		ORDER BY Name
			</cfquery>
			<cfset qryResult = tmpqry.recordcount>
		<cfcatch>
			<cfset qryResult = cfcatch.message>
		</cfcatch>
		</cftry>
		<cfreturn qryResult>
	</cffunction>

	<cffunction name="notLikeOperatorWithoutQueryparam" access="private" returntype="Any">
		<cftry >
			<cfquery name="tmpqry" dbType="query">
				SELECT *
				FROM variables.query
				WHERE  Name NOT LIKE 'test\__'  ESCAPE '\'
		 		ORDER BY Name
			</cfquery>
			<cfset qryResult = tmpqry.recordcount>
		<cfcatch>
			<cfset qryResult = cfcatch.message>
		</cfcatch>
		</cftry>
		<cfreturn qryResult>
	</cffunction>

	<cffunction name="notLikeOperatorWithQueryparam" access="private" returntype="Any">
		<cftry >
			<cfquery name="tmpqry" dbType="query">
				SELECT *
				FROM variables.query
				WHERE  Name NOT LIKE <cfqueryparam value="test\__" cfsqltype="CF_SQL_VARCHAR"> ESCAPE '\'
		 		ORDER BY Name
			</cfquery>
			<cfset qryResult = tmpqry.recordcount>
		<cfcatch>
			<cfset qryResult = cfcatch.message>
		</cfcatch>
		</cftry>
		<cfreturn qryResult>
	</cffunction>
</cfcomponent>
