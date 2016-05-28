<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">

	<cffunction name="testParagraphFormat" localMode="modern">

		<cfset 
			assertEquals(
				"yyxyyyx<P>",
				rereplace(ParagraphFormat("yy yyy"),"[[:space:]]","x","all")
		)>

		<cfset assertEquals(
				"yyxxxxxyyyx<P>",
				rereplace(ParagraphFormat("yy     yyy"),"[[:space:]]","x","all")
		)>

		<cfset assertEquals(
				"yyyyyx<P>",
				rereplace(ParagraphFormat("yyyyy"),"[[:space:]]","x","all")
		)>

	</cffunction>
	
</cfcomponent>
