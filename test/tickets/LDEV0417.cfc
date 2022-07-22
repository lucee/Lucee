<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq">
	<cfscript>

		function beforeAll(){
			qry = querynew("id,name","Integer,Varchar", [ [1,"One"], [2,"Two"], [3,"Three"], [4,"four"], [5,"five"], [6,"six"], [7,"seven"], [8,"eight"] ])
		}

		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-417", function() {
				it(title="checking cfquery tag with attribute maxrows and cachedwithin='request'", body = function( currentSpec ) {
					var result = cachedwithinRequest();
					expect(result).toBe("6");
				});

				it(title="checking cfquery tag with attribute maxrows and cachedwithin='timespan'", body = function( currentSpec ) {
					var result = cachedwithintimespan();
					expect(result).toBe("7");
				});
			});
		}
	</cfscript>

	<cffunction name="cachedwithinRequest" access="private" returntype="numeric">
		<cfquery name="getsRecords" dbtype="query" cachedwithin="request" maxrows="3">
			Select id, name From qry
		</cfquery>
		<cfquery name="getRecords" dbtype="query" cachedwithin="request" maxrows="6">
			Select id, name From qry
		</cfquery>
		<cfset getcount2 = getRecords.RECORDCOUNT>
		<cfreturn getcount2>
	</cffunction>

	<cffunction name="cachedwithintimespan" access="private" returntype="numeric">
		<cfquery name="getsRecords" dbtype="query" cachedwithin="#createTimespan(0,0,0,10)#" maxrows="4">
			Select id, name From qry
		</cfquery>
		<cfquery name="getRecords" dbtype="query" cachedwithin="#createTimespan(0,0,0,10)#" maxrows="7">
			Select id, name From qry
		</cfquery>
		<cfset getcount2 = getRecords.RECORDCOUNT>
		<cfreturn getcount2>
	</cffunction>
</cfcomponent>
