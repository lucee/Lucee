<cfparam name="form.scene" default="0">
<cfif FORM.scene EQ 1 >
	<cfset inputHTML= "<b>Lucee Starts Works on getSafeHTML()</b>">
	<cfset result = isSafeHTML(inputHTML)>
	<cfoutput>#result#</cfoutput>
<cfelse>
	<cfsavecontent variable="test">
		This is how <b>html</b>. Even <i>more</i> html!<br/>
		<iframe src="http://www.cnn.com"></iframe>
	</cfsavecontent>

	<cfset path = getDirectoryFromPath(getCurrenttemplatepath())>
	<cfset result = isSafeHTML(test, "#path#antisamy-slashdot-1.4.3.xml")>
	<cfoutput>#result#</cfoutput>
</cfif>




