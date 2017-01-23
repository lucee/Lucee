<cfset obj =new HelloWorld()>
<cfset dynInstnace = createDynamicProxy(obj, ["MyInterface"])> 
<!--- <cfdump var="#dynInstnace#" /> --->
<cfset result = IsInstanceOf(dynInstnace , 'MyInterface')>
<cfoutput>#result#</cfoutput>
