<cfcomponent displayname="base" accessors="true" output="no">

	<cfproperty type="String" name="tagName">
	<cfproperty type="Array" name="params" >
	<cfproperty type="Array" name="parts" >
	<cfproperty type="Struct" name="attributes" >

	<cfscript>
	/*
	 * Store the attributes added to the instance 
	 */
	variables.attributes = {};
	
	/*
 	 * Internal params storage
 	 */	 
	variables.params = [];	
	variables.parts = [];
	variables.tagname = "";			
	</cfscript>
	
	<!--- 
	init
	 --->
	<cffunction name="init" returntype="Base" access="public" output="false">
		<cfscript>
		setAttributes(argumentCollection=arguments);
		return this;		
		</cfscript>		
	</cffunction>

	
	<!--- 
	addParam
	 --->
	<cffunction name="addParam" returntype="Base" output="false" access="public" 
				hint="Add a new param">
		<cfscript>
		ArrayAppend(variables.params,arguments);
		return this;		
		</cfscript>
	</cffunction>


	<!--- 
	clearParams
	 --->
	<cffunction name="clearParams" returntype="Base" output="false" access="public" hint="Clear the stored params Array">
		<cfscript>
		variables.params = [];
		return this;	
		</cfscript>	
	</cffunction>

	<!--- 
	addPart
	 --->
	<cffunction name="addPart" returntype="Base" output="false" access="public">
		<cfscript>
		ArrayAppend(variables.parts,arguments);
		return this;		
		</cfscript>
	</cffunction>	

	<!--- 
	clearParts
	 --->
	<cffunction name="clearParts" returntype="Base" output="false" access="public">
		<cfscript>
		variables.parts = [];
	    return this;		
		</cfscript>
	</cffunction>	

	<!--- 
	setAttributes
	 --->
	<cffunction name="setAttributes" returntype="Base" output="false" access="public">
		<cfscript>
		StructAppend(variables.attributes, arguments, true);
		return this;		
		</cfscript>
	</cffunction>

	<!--- 
	clearAttributes
	 --->
	<cffunction name="clearAttributes" returntype="Base" output="false" access="public">
		<cfscript>
		variables.attributes = {};
		return this;		
		</cfscript>
	</cffunction>

	<!--- 
	clear
	 --->
	<cffunction name="clear" returntype="Base" output="false" access="public">
		<cfscript>
		clearAttributes();
		clearParams();
		clearParts();
		return this;		
		</cfscript>
	</cffunction>	

	<!--- 
	getSupportedTagAttributes
	 --->
	<cffunction name="getSupportedTagAttributes" returntype="Struct" output="false" access="public">
		<cfscript>
		return getTagData("cf",getTagName());
		</cfscript>
	</cffunction>	
	
	<!--- 
	invoke Tag 
	--->
	<cffunction name="invokeTag" output="false" access="private" returntype="any" hint="invokes the service tag">
		<cfset var tagname = getTagName()>
		<cfset var tagAttributes = getAttributes()>
		<cfset var tagParams = getParams()>	
		<cfset var resultVar = "">
		<cfset var result = new Result()>
		<cfset var tagResult = "">

		<!--- Makes the attributes available in local scope. Es : query of queries --->
		<cfset structAppend(local,tagAttributes,true)>
		
		<cfswitch expression="#tagname#">
			
			<!--- cfquery --->
			<cfcase value="query">
								
				<!--- get the query array to loop --->
				<cfset var qArray = getQArray()>
				<!--- declare the query local var --->
				<cfset var q = "">
				
				<cfquery name="q" attributeCollection="#tagAttributes#" result="tagResult">
					<cfloop array="#qArray#" index="Local.item"><!---
						!---><cfif structKeyExists(item,'type') and item.type eq 'string'><!---
							!--->#preserveSingleQuotes(item.value)#<!---
						!---><cfelse><!---
							!---><cfqueryparam attributecollection="#item#"><!---
						!---></cfif></cfloop>
				</cfquery>
				
				<cfset result.setResult(q)>			
				<cfset result.setPrefix(tagResult)>
				
				<cfreturn result>
			</cfcase>

			<!--- cfftp --->
			<cfcase value="ftp">
				
				<!--- 
				If action = "listdir" we need to provide a name to the cfftp tag where will
				be stored the returned query. The recordset will be passed then to the Result
				Object.
				 --->
				<cfif structkeyExists(tagAttributes,'action') and tagAttributes.action eq 'listdir'>
					<cfset tagAttributes.name = 'q' >
					<cfset var q = "">
				</cfif>
				
				<cfftp attributeCollection="#tagAttributes#" result="tagResult"/>
				
				<cfif tagAttributes["action"] eq "listdir">
	                  <cfset result.setResult(q)>
				</cfif>
								
				<cfset result.setPrefix(tagResult)>
				
			</cfcase>
			
			<!--- cfhttp --->
			<cfcase value="http">
				
				<cfhttp attributeCollection="#tagAttributes#" result="tagResult">
					<cfloop array="#tagParams#" index="param">
						<cfhttpParam attributeCollection="#param#">
					</cfloop>
				</cfhttp>
				
				<cfif structkeyexists(tagAttributes,"name") and tagAttributes["name"] neq "">
	                  <cfset result.setResult(StructFind(variables,tagAttributes["name"]))>
				</cfif>
				<cfset result.setPrefix(tagResult)>
				
			</cfcase>
			
			<!--- cfmail --->
			<cfcase value="mail">
				<cfset var body = "">
				<cfif StructKeyExists(tagAttributes, "body")>
					<cfset body = tagAttributes.body>
					<cfset Structdelete(tagAttributes, "body")>
				</cfif>
				<cfmail attributeCollection="#tagAttributes#">#body#<!---							
				---><cfloop array="#tagParams#" index="param"><!---
                        ---><cfmailparam attributeCollection="#param#"><!---
                  ---></cfloop><!---
				
				---><cfloop array="#variables.parts#" index="part"><!---
					---><cfset partbody = ""><!---
                        ---><cfif structkeyexists(part,"body")><!---
                             ---><cfset partbody = part["body"]><!---
                             ---><cfset structdelete(part,"body")><!---
                        ---></cfif><!---
                        ---><cfmailpart attributeCollection="#part#">#partbody#</cfmailpart><!---
                    ---></cfloop><!---
				---></cfmail>
			
				<cfreturn this/>
			</cfcase>

			<!--- feed --->
			<cfcase value="feed">
				
				<!--- 
				fields are optional in read mode
				 --->
				<cfif tagAttributes.action eq 'read'>
					<cfset tagAttributes.query = 'query'>
					<cfset tagAttributes.name = 'name'>
					<cfset tagAttributes.properties = 'properties'>
				</cfif>
				
				<!--- the xmlvar is forced for both actions --->
				<cfset tagAttributes.xmlvar = 'xmlvar'>																		
								
				<cffeed attributeCollection="#tagAttributes#">	
				
				<cfswitch expression="#tagAttributes.action#">
					
					<cfcase value="read">
						
						<cfset result = {
							name = name,
							query = query,
							properties = properties,
							xmlvar = xmlvar					
						}>			
							
					</cfcase>
				
					<cfcase value="create">
						
						<cfset result = xmlvar>
						
					</cfcase>
					
				</cfswitch>

			</cfcase>		
		
		</cfswitch>
		
		<cfreturn result>
				
	</cffunction>
	
	<!--- 
	onMissingMethod
	 --->	
	<cffunction name="onMissingMethod" output="false" access="public" returntype="any"
				hint="Allow general get() set() method on the attributes struct and on extra values ( like mail body )">
		<cfargument name="methodname" type="string">
		<cfargument name="methodArguments" type="Array">
		
		<cfscript>
			var attrName = mid( arguments.methodname, 4 );
			var methodType = left( arguments.methodname, 3 );
			var tagname = getTagName();
			var supportedTagAttributes = getSupportedTagAttributes().attributes;
			var tagAttributes = getAttributes();
			
			var lAllowedExtra = "";
			
			switch(tagName){
				case "mail":
					lAllowedExtra = "body";
					break;
				case "query":
					lAllowedExtra = "sql";	
					break;			
			}

			if(methodType EQ "get" && (StructKeyExists(supportedTagAttributes, attrName) || ListFindNoCase(lAllowedExtra, attrName))){
				if(StructKeyExists(tagAttributes, attrName)){
					return tagAttributes[attrName];
				}
				else{
					return "";
				}
			}
			
			if(methodType EQ "set" && (StructKeyExists(supportedTagAttributes, attrName) || ListFindNoCase(lAllowedExtra, attrName))){
				variables.attributes[attrName] = arguments.methodArguments[1];
				return this;
			}
			
			throw("There is no method with the name #arguments.methodName#", "expression");	
		</cfscript>
	</cffunction>
	
</cfcomponent>