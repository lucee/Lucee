<!--- create no output here!!! 
<cfset systemOutput(url,1,1)>---><cfsetting showdebugoutput="false"><cfadmin
		action="updateAdminMode"
		type="#url.adminType#"
		password="#session["password"&url.adminType]#"
		mode="#url.adminMode#"
		merge="#!isNull(url.switch) && url.switch=="merge"#"
		keep="#!isNull(url.keep)#">