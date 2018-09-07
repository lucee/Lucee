<cfif thistag.executionMode EQ "start">
	<cfsilent>
		<cfparam name="request.tpnames" type="struct" default="#struct()#">
		<cfif structkeyExists(request.tpnames,attributes.name)>
			<cfthrow message="ambigous tabbedPane name #attributes.name#">
		</cfif>
		<cfset request.tpnames[attributes.name]=1>
		<cfset actionName=attributes.name&"_tab">
		<cfparam name="attributes.name" default="">
		<cfif isDefined('url.'&actionName)>
			<cfset cTab=url[actionName]>
		<cfelse>
			<cfset cTab=attributes.default>
		</cfif>
		<cfset attributes.ctab=ctab>
		<cfset request._ctab=ctab>
		<cfset baseurl=request.self>
		<cfset baseurl=cgi.query_string>

		<cfif isDefined('url.#actionName#')>
			<cfset qs="">
			<cfloop collection="#url#" item="key">
				<cfif key NEQ actionName><cfset qs=qs&key&"="&url[key]&"&"></cfif>
			</cfloop>
			<cfset baseurl=request.self&"?"&qs&"#actionName#=">
		<cfelseif len(cgi.query_string)>
			<cfset baseurl=request.self&"?"&cgi.query_string&"&#actionName#=">
		<cfelse>
			<cfset baseurl=request.self&"?#actionName#=">
		</cfif>
	</cfsilent>

		<style type="text/css">
			div.tabs {
				height: 22px;
				margin-bottom:-1px;
				z-index:2;
			}
			div.tabs a {
				float:left;
				background-color: #bf4f36;
				color : white;	
				border-bottom-color:#fff;
				font-weight:bold;
				padding: 3px 23px;
				margin: 0 0 0 5px;
				font-size: 12px;
    			text-decoration: none; 
			}
			div.tabs a.activTab {
				border: 1px solid #ddd;
				border-bottom-color:#ccc;
				background-color: #efede5;
    			border-bottom-color: #efede5 !important;
    			color : black !important;
			}
			div.tabcontainer {
				border: 1px solid #ccc;
				padding:20px 10px;
			}
		</style>
	<cfoutput>
		<div class="tabs">
			<cfset wasActive=false>
			<cfset count=0>
			<cfloop collection="#attributes.tabs#" item="key">
				<cfset count=count+1>
				<!--- Inactiv --->
				<cfif key NEQ ctab>
					<a class="inactivTab" href="#baseurl##key#">#attributes.tabs[key]#</a>
					<cfset wasActive=false>
				<cfelse>	
					<a class="activTab" href="#baseurl##key#">#attributes.tabs[key]#</a>
					<cfset wasActive=true>
				</cfif>
			</cfloop>
		</div>
		<div class="tabcontainer">
	</cfoutput>
<cfelse>
	</div>
</cfif>	
