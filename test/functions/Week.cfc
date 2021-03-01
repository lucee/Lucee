<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">

	<cffunction name="testLasrWeekOfYear" localMode="modern">
<cfscript>
	assertEquals(52,Week("{ts '2020-12-26 0:00:00'}"));
	assertEquals(53,Week("{ts '2020-12-27 0:00:00'}"));
</cfscript>
	</cffunction>


</cfcomponent>