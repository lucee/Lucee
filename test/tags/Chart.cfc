<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">

 	<!---
	This does not test the quality of the result, only if it works without exception ...
 	 --->

	<cffunction name="testBar" localmode="true">
		<cfsilent>
			<cfchart
			   format="png"
			   scalefrom="0"
			   scaleto="1200000">
			  <cfchartseries
			      type="bar"
			      serieslabel="Website Traffic 2006"
			      seriescolor="blue">
			    <cfchartdata item="January" value="503100">
			    <cfchartdata item="February" value="720310">
			    <cfchartdata item="March" value="688700">
			    <cfchartdata item="April" value="986500">
			    <cfchartdata item="May" value="1063911">
			    <cfchartdata item="June" value="1125123">
			  </cfchartseries>
			</cfchart>
		</cfsilent>
	</cffunction>


	<cffunction name="testBarMultipleSeries" localmode="true">
		<cfsilent>
<cfchart
         format="png"
         scalefrom="0"
         scaleto="1200000">
	<cfchartseries
	             type="bar"
	             serieslabel="Website Traffic 2005"
	             seriescolor="##ffcc00">
		<cfchartdata item="January" value="245200">
		<cfchartdata item="February" value="420560">
		<cfchartdata item="March" value="488710">
		<cfchartdata item="April" value="686320">
		<cfchartdata item="May" value="763450">
		<cfchartdata item="June" value="825562">
	</cfchartseries>
	<cfchartseries
	             type="bar"
	             serieslabel="Website Traffic 2006"
	             seriescolor="blue">
		<cfchartdata item="January" value="503100">
		<cfchartdata item="February" value="720310">
		<cfchartdata item="March" value="688700">
		<cfchartdata item="April" value="986500">
		<cfchartdata item="May" value="1063911">
		<cfchartdata item="June" value="1125123">
	</cfchartseries>
</cfchart>
		</cfsilent>
	</cffunction>


	<cffunction name="testBar3D" localmode="true">
		<cfsilent>
<cfchart
         format="gif"
         scalefrom="0"
         scaleto="1200000"
         show3d="Yes">
	<cfchartseries
	             type="bar"
	             serieslabel="Website Traffic 2005"
	             seriescolor="##ffcc00">
		<cfchartdata item="January" value="245200">
		<cfchartdata item="February" value="420560">
		<cfchartdata item="March" value="488710">
		<cfchartdata item="April" value="686320">
		<cfchartdata item="May" value="763450">
		<cfchartdata item="June" value="825562">
	</cfchartseries>
	<cfchartseries
	             type="bar"
	             serieslabel="Website Traffic 2006"
	             seriescolor="blue">
		<cfchartdata item="January" value="503100">
		<cfchartdata item="February" value="720310">
		<cfchartdata item="March" value="688700">
		<cfchartdata item="April" value="986500">
		<cfchartdata item="May" value="1063911">
		<cfchartdata item="June" value="1125123">
	</cfchartseries>
</cfchart>
		</cfsilent>
	</cffunction>



	<cffunction name="testPie" localmode="true">
		<cfsilent>
<cfchart
         format="png"
         scalefrom="0"
         scaleto="1200000"
         pieslicestyle="solid">
	<cfchartseries
	             type="pie"
	             serieslabel="Website Traffic 2006"
	             seriescolor="blue">
		<cfchartdata item="January" value="503100">
		<cfchartdata item="February" value="720310">
		<cfchartdata item="March" value="688700">
		<cfchartdata item="April" value="986500">
		<cfchartdata item="May" value="1063911">
		<cfchartdata item="June" value="1125123">
	</cfchartseries>
</cfchart>
		</cfsilent>
	</cffunction>


<cfscript>
	public function beforeTests(){
		
	}

	public function afterTests(){
		
	}
</cfscript>



</cfcomponent>