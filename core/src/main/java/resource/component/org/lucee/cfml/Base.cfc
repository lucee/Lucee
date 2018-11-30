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

	public Base function init() {
		setAttributes(argumentCollection=arguments);
		return this;	
	}

	/**
	* Add a new param
	*/
	public Base function addParam() {
		ArrayAppend(variables.params,arguments);
		return this;	
	}

	/**
	* Clear the stored params Array
	*/
	public Base function clearParams() {
		variables.params = [];
		return this;
	}

	/**
	* add a new part
	*/
	public Base function addPart() {
		ArrayAppend(variables.parts,arguments);
		return this;
	}

	/**
	* Clear the stored parts Array
	*/
	public Base function clearParts() {
		variables.parts = [];
	    return this;	
	}

	public Base function setAttributes() {
		StructAppend(variables.attributes, arguments, true);
		return this;	
	}

	public Base function clearAttributes() {
		variables.attributes = {};
		return this;	
	}

	public Base function clear() {
		clearAttributes();
		clearParams();
		clearParts();
		return this;	
	}

	public Struct function getSupportedTagAttributes() {
		return getTagData("cf",getTagName());	
	}


	</cfscript>
	<!--- 
	invoke Tag 
	--->
	<cffunction name="invokeTag" output="false" access="private" returntype="any" hint="invokes the service tag">
		<cfset local.tagname = getTagName()>
		<cfset local.tagAttributes = getAttributes()>
		<cfset local.tagParams = getParams()>	
		<cfset local.resultVar = "">
		<cfset local.result = new Result()>
		
		<!--- Makes the attributes available in local scope. Es : query of queries --->
		<cfset structAppend(local,tagAttributes,true)>

		<cfswitch expression="#tagname#">

			<!--- cfquery --->
			<cfcase value="query">

				<!--- get the query array to loop --->
				<cfset local.qArray = getQArray()>
				<!--- declare the query local var --->

				<cfquery name="local.___q" attributeCollection="#tagAttributes#" result="local.tagResult">
					<cfloop array="#local.qArray#" index="Local.item"><!---
						!---><cfif structKeyExists(item,'type') and item.type eq 'string'><!---
							!--->#preserveSingleQuotes(item.value)#<!---
						!---><cfelse><!---
							!---><cfqueryparam attributecollection="#item#"><!---
						!---></cfif></cfloop>
				</cfquery>

				<cfif !isNull(local.___q)><cfset result.setResult(local.___q)></cfif>
				<cfif !isNull(local.tagResult)><cfset result.setPrefix(local.tagResult)></cfif>

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
					<cfset tagAttributes.name = 'local.___q' >
				</cfif>
				<cfftp attributeCollection="#tagAttributes#" result="local.tagResult"/>
				<cfif tagAttributes["action"] eq "listdir">
	                  <cfif !isNull(local.___q)><cfset result.setResult(local.___q)></cfif>
				</cfif>
				<cfif !isNull(local.tagResult)><cfset result.setPrefix(local.tagResult)></cfif>
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
				<cfset local.body = "">
				<cfif StructKeyExists(tagAttributes, "body")>
					<cfset local.body = tagAttributes.body>
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
			local.attrName = mid( arguments.methodname, 4 );
			local.methodType = left( arguments.methodname, 3 );
			local.tagname = getTagName();
			local.supportedTagAttributes = getSupportedTagAttributes().attributes;
			local.tagAttributes = getAttributes();
			local.lAllowedExtra = "";
			
			switch(tagName){
				case "mail":
					local.lAllowedExtra = "body";
					break;
				case "query":
					local.lAllowedExtra = "sql";	
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