<cfset propTest = CreateObject("component", "test" )>
<cfset propTest.setTest("property_name-test")>
<cfoutput>#propTest.getTest()#</cfoutput>
