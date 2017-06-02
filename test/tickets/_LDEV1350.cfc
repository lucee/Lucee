<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run(){
			describe( title="Test suite for LDEV-1350", body=function(){
				it(title="checking cfchartseries with query cell as empty string", body=function(){
					 var result = testChartSeries();
					expect(result).toBe("false");
				});
			});
		}
	</cfscript>

	<cffunction name="testChartSeries" access="private" returntype="string" returnformat="plain">
		<cfset chartData = queryNew("")>
		<cfset queryAddColumn(chartData, "id", "Integer", [1,2])>
		<cfset queryAddColumn(chartData, "series1", "Integer", [])>
		<cfset queryAddColumn(chartData, "series2", "Integer", [5,10])>
		<cfset hasError = "false">

		<cftry>
			<cfchart chartwidth="600" chartheight="400" seriesplacement="stacked" format="png">
				<cfchartseries query="chartData" type="line" itemcolumn="id" valuecolumn="series1">
				<cfchartseries query="chartData" type="bar" itemcolumn="id" valuecolumn="series2">
			</cfchart>
		<cfcatch>
			<cfset hasError = cfcatch.message>
		</cfcatch>
		</cftry>

		<cfreturn hasError>
	</cffunction>
</cfcomponent>	