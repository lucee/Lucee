<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">

	<cffunction name="testParagraphFormatMember" localMode="modern">

		<cfset 
			assertEquals(
				"yyxyyyx<P>",
				rereplace("yy yyy".ParagraphFormat(),"\s","x","all")
			)>

	</cffunction>

	<cffunction name="testParagraphFormat" localMode="modern">

		<cfset 
			assertEquals(
				"yyxyyyx<P>",
				rereplace(ParagraphFormat("yy yyy"),"\s","x","all")
		)>

		<cfset assertEquals(
				"yyxxxxxyyyx<P>",
				rereplace(ParagraphFormat("yy     yyy"),"\s","x","all")
		)>

		<cfset assertEquals(
				"yyyyyx<P>",
				rereplace(ParagraphFormat("yyyyy"),"\s","x","all")
		)>

	</cffunction>
	
</cfcomponent>
