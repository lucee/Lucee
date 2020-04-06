<cfset propTest = CreateObject("component", "test" )>
<cfset propTest.setTest("property_name-test")>
<cfdump var="#propTest#" />
<cfoutput>#propTest.getTest()#</cfoutput>
