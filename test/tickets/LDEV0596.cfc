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
			querySetCell(MyQuery, 'Manager', 'Jane Doe');
			querySetCell(MyQuery, 'Employee', 'Chewbacca');

			resultQuery = QueryExecute(
				options = {
					dbtype: 'query'
				},
				sql = "SELECT Manager, Employee
				FROM MyQuery
				ORDER BY Manager, Employee"
			);

			ManagerCount = QueryExecute(
				options = {
					dbtype: 'query'
				},
				sql = "SELECT DISTINCT Manager FROM MyQuery"
			);
		}
		function run( testResults , testBox ) {
			describe( 'attribute with cfoutput', function() {
				it( title='attributes directly with cfoutput' , body=function(){
					directAttributesforcfoutput();
				});
				it( title='attributes used as attributes collection in cfoutput' ,  body=function(){
					attributesCollectionforcfoutput();
				});
			});
			describe( 'attributes with cfloop', function() {
				it( title='attributes used directly in cfloop',  body=function(){
					directAttributesforcfloop();
				});
				it( title='attributes used as attributes collection in cfloop',  body=function(){
					attributesCollectionforcfloop();
				});
			});
		}
	</cfscript>
	<cffunction name="directAttributesforcfoutput">
		<cfset  countofManager = 0>
		<cfset  listofEmployees = 0>
		<cfoutput query="resultQuery" group="Manager">
			<cfset  countofManager++>
				<cfoutput>
					<cfset listofEmployees++>
				</cfoutput>
		</cfoutput>
		<cfset expect( countofManager EQ ManagerCount.RecordCount && listofEmployees EQ resultQuery.RecordCount ).toBeTrue()>
	</cffunction>
	<cffunction name="attributesCollectionforcfoutput">
		<cfset OutputAttributes = {query='Results', group='Manager'}>
		<cfset  countofManager = 0>
		<cfset  listofEmployees = 0>
		<cfoutput  attributeCollection="#OutputAttributes#">
			<cfset  countofManager++>
				<cfoutput>
					<cfset listofEmployees++>
				</cfoutput>
		</cfoutput>
		<cfset expect( countofManager EQ ManagerCount.RecordCount && listofEmployees EQ resultQuery.RecordCount ).toBeTrue()>
	</cffunction>
	<cffunction name="directAttributesforcfloop">
		<cfset  countofManager = 0>
		<cfset  listofEmployees = 0>
		<cfloop query="resultQuery" group="Manager">
			<cfset  countofManager++>
			<cfloop>
				<cfset listofEmployees++>
			</cfloop>
		</cfloop>
		<cfset expect( countofManager EQ ManagerCount.RecordCount && listofEmployees EQ resultQuery.RecordCount ).toBeTrue()>
	</cffunction>
	<cffunction name="attributesCollectionforcfloop">
		<cfset OutputAttributes = {query='Results', group='Manager'}>
		<cfset  countofManager = 0>
		<cfset  listofEmployees = 0>
		<cfloop attributeCollection="#OutputAttributes#">
			<cfset  countofManager++>
			<cfloop>
				<cfset listofEmployees++>
			</cfloop>
		</cfloop>
		<cfset expect( countofManager EQ ManagerCount.RecordCount && listofEmployees EQ resultQuery.RecordCount ).toBeTrue()>
	</cffunction>
</cfcomponent>