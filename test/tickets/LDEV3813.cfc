<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="guard">

	<cffunction name="testEncodedVersusPreserved" localmode="true">
		<cfset str="<b>Tom & 'Jerry'</b>">
		<cfsavecontent variable="c"><cfoutput encodeFor="html">#str#|#preserveEncoding(str)#</cfoutput></cfsavecontent>
		<cfset assertNotEquals(str, encodeForHTML(str))>
		<cfset assertEquals(encodeForHTML(str) & "|" & str, c)>
	</cffunction>

	<cffunction name="testPreservedFirstAndLast" localmode="true">
		<cfset str="<i>x</i>">
		<cfsavecontent variable="c"><cfoutput encodeFor="html">#preserveEncoding(str)#|#str#|#preserveEncoding(str)#</cfoutput></cfsavecontent>
		<cfset assertEquals(str & "|" & encodeForHTML(str) & "|" & str, c)>
	</cffunction>

	<cffunction name="testFunctionNameIsCaseInsensitive" localmode="true">
		<cfset str="<i>x</i>">
		<cfsavecontent variable="c"><cfoutput encodeFor="html">#PRESERVEENCODING(str)#|#PreserveEncoding(str)#|#preserveencoding(str)#|#pReSeRvEeNcOdInG(str)#</cfoutput></cfsavecontent>
		<cfset assertEquals(str & "|" & str & "|" & str & "|" & str, c)>
	</cffunction>

	<cffunction name="testOtherEncodeForTypes" localmode="true">
		<cfset str="<a b>">
		<cfsavecontent variable="c"><cfoutput encodeFor="url">#str#|#preserveEncoding(str)#</cfoutput></cfsavecontent>
		<cfset assertEquals(encodeForURL(str) & "|" & str, c)>
		<cfsavecontent variable="c"><cfoutput encodeFor="javascript">#str#|#preserveEncoding(str)#</cfoutput></cfsavecontent>
		<cfset assertEquals(encodeForJavaScript(str) & "|" & str, c)>
		<cfsavecontent variable="c"><cfoutput encodeFor="xml">#str#|#preserveEncoding(str)#</cfoutput></cfsavecontent>
		<cfset assertEquals(encodeForXML(str) & "|" & str, c)>
	</cffunction>

	<cffunction name="testDynamicEncodeForType" localmode="true">
		<cfset str="<a b>">
		<cfset et="url">
		<cfsavecontent variable="c"><cfoutput encodeFor="#et#">#str#|#preserveEncoding(str)#</cfoutput></cfsavecontent>
		<cfset assertEquals(encodeForURL(str) & "|" & str, c)>
	</cffunction>

	<cffunction name="testNestedInsideOtherTags" localmode="true">
		<cfset str="<b>">
		<cfset yes=true>
		<cfsavecontent variable="c"><cfoutput encodeFor="html"><cfif yes>#str#|#preserveEncoding(str)#<cfelse>no</cfif><cfloop from="1" to="2" index="i">;#i#:#str#|#preserveEncoding(str)#</cfloop></cfoutput></cfsavecontent>
		<cfset e=encodeForHTML(str)>
		<cfset assertEquals(e & "|" & str & ";1:" & e & "|" & str & ";2:" & e & "|" & str, c)>
	</cffunction>

	<cffunction name="testQueryOutput" localmode="true">
		<cfset q=queryNew("name","varchar",[["<i>a</i>"],["<u>b</u>"]])>
		<cfsavecontent variable="c"><cfoutput query="q" encodeFor="html">#q.name#|#preserveEncoding(q.name)#;</cfoutput></cfsavecontent>
		<cfset assertEquals(encodeForHTML("<i>a</i>") & "|<i>a</i>;" & encodeForHTML("<u>b</u>") & "|<u>b</u>;", c)>
	</cffunction>

	<cffunction name="testNestedFunctionCalls" localmode="true">
		<cfset str="<b>&</b>">
		<cfset e=encodeForHTML(str)>
		<!--- an encoded value kept as it is, no double encoding --->
		<cfsavecontent variable="c"><cfoutput encodeFor="html">#preserveEncoding(encodeForHTML(str))#</cfoutput></cfsavecontent>
		<cfset assertEquals(e, c)>
		<!--- an expression as argument --->
		<cfsavecontent variable="c"><cfoutput encodeFor="html">#preserveEncoding("<" & "b" & ">")#</cfoutput></cfsavecontent>
		<cfset assertEquals("<b>", c)>
		<!--- preserveEncoding is only effective when it is the whole expression, any other expression is encoded --->
		<cfsavecontent variable="c"><cfoutput encodeFor="html">#preserveEncoding(str) & "<"#</cfoutput></cfsavecontent>
		<cfset assertEquals(encodeForHTML(str & "<"), c)>
	</cffunction>

	<cffunction name="testWithoutEncodeForAttribute" localmode="true">
		<cfset str="<b>&</b>">
		<cfsavecontent variable="c"><cfoutput>#str#|#preserveEncoding(str)#</cfoutput></cfsavecontent>
		<cfset assertEquals(str & "|" & str, c)>
	</cffunction>

	<cffunction name="testOutsideOfCfoutput" localmode="true">
		<cfset str="<b>&</b>">
		<cfset assertEquals(str, preserveEncoding(str))>
		<cfset assertEquals(42, preserveEncoding(42))>
		<cfset assertEquals("", preserveEncoding(""))>
		<cfset assertTrue(preserveEncoding(true))>
		<cfset s={a:1}>
		<cfset assertEquals(1, preserveEncoding(s).a)>
		<cfset assertEquals(3, arrayLen(preserveEncoding([1,2,3])))>
		<cfset assertEquals(encodeForHTML(str), encodeForHTML(preserveEncoding(str)))>
	</cffunction>

	<cffunction name="testNumbersAreOutputUntouched" localmode="true">
		<cfset n=1234.5>
		<cfsavecontent variable="c"><cfoutput encodeFor="html">#preserveEncoding(n)#|#n#</cfoutput></cfsavecontent>
		<cfset assertEquals("1234.5|1234.5", c)>
	</cffunction>

</cfcomponent>
