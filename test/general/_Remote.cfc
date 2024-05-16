<cfcomponent displayName="tester" hint="Test for SOAP headers" labels="xml"> 
 
 
<cffunction name="testEmpty"  
    access="remote" 
    output="false"  
    returntype="string"  
    > 
     
 
<cfreturn serializeJson(getSOAPRequest())> 
 
</cffunction> 
 
 
 
<cffunction name="addResponse"  
    access="remote" 
    output="false"  
    returntype="string"> 
  	<cftry>
    <!--- Add a header as a string ---> 
	<cfset addSOAPResponseHeader("http://www.tomj.org/myns", "returnheader", "AUTHORIZED VALUE", false)> 
 
    <!--- Add a second header using a CFML XML value ---> 
    <cfset doc = XmlNew()> 
    <cfset x = XmlElemNew(doc, "http://www.tomj.org/myns", "returnheader2")> 
    <cfset x.XmlText = "hey man, here I am in XML"> 
    <cfset x.XmlAttributes["xsi:type"] = "xsd:string"> 
    <cfset tmp = addSOAPResponseHeader("ignoredNameSpace", "ignoredName", x)>  
 		<cfcatch>
        	<cfset systemOutput(cfcatch,true,true)>
        </cfcatch>
    </cftry>
	<cfreturn isSoapRequest()> 
</cffunction> 
 
</cfcomponent>