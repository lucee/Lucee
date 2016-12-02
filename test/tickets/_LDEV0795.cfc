<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run(){
			describe( title="Test suite for LDEV-795", body=function(){
				it(title="Checking cfchart with different type of series", body=function(){
					result = testChartSeries();
					expect(result.trim()).toBe("true|true|true|true");
				});
			});
		}
	</cfscript>

	<cffunction name="testChartSeries" access="private" returntype="string" returnformat="plain">
		<cfset chartData = queryNew("")>
		<cfset queryAddColumn(chartData, "id", "Integer", [1,2])>
		<cfset queryAddColumn(chartData, "series1", "Integer", [100,90])>
		<cfset queryAddColumn(chartData, "series2", "Integer", [20,10])>

		<cfsavecontent variable="chartContent">
		<cfchart chartwidth="640" chartheight="480" seriesplacement="stacked" format="png">
			<cfchartseries query="chartData" type="line" itemcolumn="id" valuecolumn="series1">
			<cfchartseries query="chartData" type="bar" itemcolumn="id" valuecolumn="series2">
		</cfchart>
		</cfsavecontent>
		<cfset startPos = findNoCase("<map ", chartContent)>
		<cfset endPos = findNoCase("</map>", chartContent)>
		<cfset xmlString = mid(chartContent, startPos, endPos-startPos+6)>
		<cfset xmlContent = xmlParse(xmlString)>

		<cfset loopingArr = []>
		<cfset loopingArr = xmlContent.xmlRoot.xmlChildren>

		<cfset result = "">
		<cfloop array="#loopingArr#" index="idx">
			<cfset res = false>
			<cfif idx.xmlAttributes.title EQ "20 (2,1)" OR idx.xmlAttributes.title EQ "10 (2,2)">
				<cfif idx.xmlAttributes.shape EQ "rect">
					<cfset res = true>
				<cfelse>
					<cfset res = false>
				</cfif>
			<cfelseif idx.xmlAttributes.title EQ "100 (1,1)" OR idx.xmlAttributes.title EQ "90 (1,2)">
				<cfif idx.xmlAttributes.shape EQ "poly">
					<cfset res = true>
				<cfelse>
					<cfset res = false>
				</cfif>
			</cfif>
			<cfset result = listAppend(result, res, "|")>
		</cfloop>
		<cfreturn result>
	</cffunction>
</cfcomponent>