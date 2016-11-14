<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function beforeAll(){
			MyQuery = queryNew('Manager, Employee', 'varchar,varchar');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'Bill Smith');
			querySetCell(MyQuery, 'Employee', 'Susan Jones');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'Bill Smith');
			querySetCell(MyQuery, 'Employee', 'Robert Jackson');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'saravanan');
			querySetCell(MyQuery, 'Employee', 'pothy');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'saravanan');
			querySetCell(MyQuery, 'Employee', 'Chewbacca');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'pothy');
			querySetCell(MyQuery, 'Employee', 'mitrahsoft');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'pothy');
			querySetCell(MyQuery, 'Employee', 'Bill Smith');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'mitrahsoft');
			querySetCell(MyQuery, 'Employee', 'mitrahsoft');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'mitrahsoft');
			querySetCell(MyQuery, 'Employee', 'Bill Smith');

			resultQuery= QueryExecute(
				options = {
					dbtype: 'query'
				},
				sql = "SELECT Manager, Employee
				FROM MyQuery
				ORDER BY Manager, Employee"
			);
		}
		function run( testResults , testBox ) {
			describe( "maxrows with parameters", function() {
				it( title="max rows EQ 1",  body=function(){
					maxrow1();
				});
				it( title="max rows EQ 2",  body=function(){
					maxrow2();
				});
				it( title="max rows EQ 3",  body=function(){
					maxrow3();
				});
				it( title="max rows EQ 4",  body=function(){
					maxrow4();
				});
			});
		}
	</cfscript>


	<!--- private functions --->
	<cffunction name="maxrow1" access="private">
		<cfset countofManager = 0>
		<cfset listofEmployees = 0>
		<cfoutput  query="resultQuery" group="Manager" maxrows="1">
			<cfset countofManager++>
			<cfoutput>
				<cfset listofEmployees++>
			</cfoutput>
		</cfoutput>
		<cfset expect(countofManager EQ 1 && listofEmployees EQ 2).toBeTrue()>
	</cffunction>
	<cffunction name="maxrow2" access="private">
		<cfset countofManager = 0>
		<cfset listofEmployees = 0>
		<cfoutput  query="resultQuery" group="Manager" maxrows="2">
			<cfset countofManager++>
			<cfoutput>
				<cfset listofEmployees++>
			</cfoutput>
		</cfoutput>
		<cfset expect(countofManager EQ 2 && listofEmployees EQ 4).toBeTrue()>
	</cffunction>
	<cffunction name="maxrow3" access="private">
		<cfset countofManager = 0>
		<cfset listofEmployees = 0>
		<cfoutput  query="resultQuery" group="Manager" maxrows="3">
			<cfset countofManager++>
			<cfoutput>
				<cfset listofEmployees++>
			</cfoutput>
		</cfoutput>
		<cfset expect(countofManager EQ 3 && listofEmployees EQ 6).toBeTrue()>
	</cffunction>
	<cffunction name="maxrow4" access="private">
		<cfset countofManager = 0>
		<cfset listofEmployees = 0>
		<cfoutput  query="resultQuery" group="Manager" maxrows="4">
			<cfset countofManager++>
			<cfoutput>
				<cfset listofEmployees++>
			</cfoutput>
		</cfoutput>
		<cfset expect(countofManager EQ 4 && listofEmployees EQ 8).toBeTrue()>
	</cffunction>
</cfcomponent>