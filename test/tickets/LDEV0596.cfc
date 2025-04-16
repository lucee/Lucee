<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function beforeAll(){
			variables.MyQuery = queryNew('Manager, Employee', 'varchar,varchar');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'Bill Smith');
			querySetCell(MyQuery, 'Employee', 'Susan Jones');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'Bill Smith');
			querySetCell(MyQuery, 'Employee', 'Robert Jackson');
			queryAddRow(MyQuery);
			querySetCell(MyQuery, 'Manager', 'Jane Doe');
			querySetCell(MyQuery, 'Employee', 'Chewbacca');

			variables.resultQuery = QueryExecute(
				options = {
					dbtype: 'query'
				},
				sql = "SELECT Manager, Employee
				FROM MyQuery
				ORDER BY Manager, Employee"
			);

			variables.ManagerCount = QueryExecute(
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
				xit( title='attributes used as attributes collection in cfoutput' ,  body=function(){
					attributesCollectionforcfoutput();
				});
			});
			describe( 'attributes with cfloop', function() {
				it( title='attributes used directly in cfloop',  body=function(){
					directAttributesforcfloop();
				});
				xit( title='attributes used as attributes collection in cfloop',  body=function(){
					attributesCollectionforcfloop();
				});
			});
		}
	</cfscript>
	<cffunction name="directAttributesforcfoutput">
		<cfset var countofManager = 0>
		<cfset var listofEmployees = 0>
		<cfoutput query="resultQuery" group="Manager">
			<cfset  countofManager++>
				<cfoutput>
					<cfset listofEmployees++>
				</cfoutput>
		</cfoutput>
		<cfset expect( countofManager EQ ManagerCount.RecordCount && listofEmployees EQ resultQuery.RecordCount ).toBeTrue()>
	</cffunction>
	<cffunction name="attributesCollectionforcfoutput">
		<cfset var OutputAttributes = {query='Results', group='Manager'}>
		<cfset var countofManager = 0>
		<cfset var listofEmployees = 0>
		<cfoutput  attributeCollection="#OutputAttributes#">
			<cfset  countofManager++>
				<cfoutput>
					<cfset listofEmployees++>
				</cfoutput>
		</cfoutput>
		<cfset expect( countofManager EQ ManagerCount.RecordCount && listofEmployees EQ resultQuery.RecordCount ).toBeTrue()>
	</cffunction>
	<cffunction name="directAttributesforcfloop">
		<cfset var countofManager = 0>
		<cfset var listofEmployees = 0>
		<cfloop query="resultQuery" group="Manager">
			<cfset  countofManager++>
			<cfloop>
				<cfset listofEmployees++>
			</cfloop>
		</cfloop>
		<cfset expect( countofManager EQ ManagerCount.RecordCount && listofEmployees EQ resultQuery.RecordCount ).toBeTrue()>
	</cffunction>
	<cffunction name="attributesCollectionforcfloop">
		<cfset var OutputAttributes = {query='Results', group='Manager'}>
		<cfset var countofManager = 0>
		<cfset var listofEmployees = 0>
		<cfloop attributeCollection="#OutputAttributes#">
			<cfset  countofManager++>
			<cfloop>
				<cfset listofEmployees++>
			</cfloop>
		</cfloop>
		<cfset expect( countofManager EQ ManagerCount.RecordCount && listofEmployees EQ resultQuery.RecordCount ).toBeTrue()>
	</cffunction>
</cfcomponent>