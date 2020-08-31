<cffunction name="createMenu" returntype="array" localmode="modern">
	<cfargument name="stMenu" type="struct" required="yes">
	<cfargument name="adminType" type="string" required="yes">
	<cfset var MenuStruct = 
	array(
		struct(
			action:"overview",label:arguments.stMenu.overview.label,
			children:array(
				struct(action:"",label:arguments.stMenu.overview.overview)
			)
		),
		{
			action:"info",label:arguments.stMenu.info.label,
			children:[
				{action:"bundle",label:arguments.stMenu.info.bundle}
			]
		},
		struct(
			action:"server",label:arguments.stMenu.server.label,
			children:array(
				struct(action:"cache",label:arguments.stMenu.server.cache),
				struct(action:"compiler",label:arguments.stMenu.server.compiler),
				struct(action:"security",label:arguments.stMenu.server.security),
				struct(action:"regional",label:arguments.stMenu.server.regional),
				struct(action:"charset",label:arguments.stMenu.server.charset),
				struct(action:"scope",label:arguments.stMenu.server.scope),
				struct(action:"request",label:arguments.stMenu.server.request),
				struct(action:"output",label:arguments.stMenu.server.output),
				struct(action:"error",label:arguments.stMenu.server.error),
				struct(action:"logging",label:arguments.stMenu.server.logging),
				struct(action:"regex",label:(arguments.stMenu.server.regex?:"Regex")),
				struct(action:"export",label:arguments.stMenu.server.export),
				struct(action:"proxy",label:arguments.stMenu.server.proxy)
			)
		),
		struct(
			action:"services",label:arguments.stMenu.services.label,
			children:array(
				struct(action:"gateway",label:arguments.stMenu.services.gateway,hidden: arguments.adminType NEQ "web"),
				struct(action:"cache",label:arguments.stMenu.services.cache),
				struct(action:"datasource",label:arguments.stMenu.services.datasource),
				struct(action:"orm",label:arguments.stMenu.services.orm),
				struct(action:"search",label:arguments.stMenu.services.search,hidden: arguments.adminType NEQ "web"),
				struct(action:"mail",label:arguments.stMenu.services.mail),
				struct(action:"tasks",label:arguments.stMenu.services.tasks),
				struct(action:"schedule",label:arguments.stMenu.services.schedule,hidden:arguments.adminType NEQ "web"),
				struct(action:"update",label:arguments.stMenu.services.update,hidden:arguments.adminType EQ "web",display:true),
				struct(action:"restart",label:arguments.stMenu.services.restart,hidden:arguments.adminType EQ "web",display:true),
				struct(action:"certificates",label:arguments.stMenu.services.certificates,hidden:arguments.adminType EQ "web",display:true)
				
			)
		),
		struct(
			action:"ext",label:arguments.stMenu.extension.label,
			children:array(
				struct(action:"applications",label:arguments.stMenu.extension.applications),
				struct(action:"providers",label:arguments.stMenu.extension.providers)
			)
		),
		struct(
			action:"remote",label:arguments.stMenu.remote.label,
			children:array(
				struct(action:"securityKey",label:arguments.stMenu.remote.securityKey,hidden:!request.hasRemoteClientUsage),
				struct(action:"clients",label:arguments.stMenu.remote.clients,hidden:!request.hasRemoteClientUsage)
			)
		),
		
		struct(
			action:"resources",label:arguments.stMenu.resources.label,
			children:array(
				struct(action:"mappings",label:arguments.stMenu.resources.mappings),
				struct(action:"rest",label:isDefined('arguments.stMenu.resources.rest')?arguments.stMenu.resources.rest:'Rest'),
				struct(action:"component",label:arguments.stMenu.resources.component),
				struct(action:"customtags",label:arguments.stMenu.resources.customtags),
				struct(action:"cfx_tags",label:arguments.stMenu.resources.cfx_tags)
			)
		),
		struct(action:"debugging",label:arguments.stMenu.debugging.label,
			children:array(
				struct(action:"settings",label:arguments.stMenu.debugging.settings),
				struct(action:"templates",label:arguments.stMenu.debugging.templates),
				struct(action:"logs",label:arguments.stMenu.debugging.logs)
			)
		),
		struct(action:"security",label:arguments.stMenu.security.label,
			children:array(
				struct(action:"access",label:arguments.stMenu.security.access,hidden:arguments.adminType NEQ "server"),
				struct(action:"password",label:arguments.stMenu.security.password,display:true)
				,struct(action:"serial",label:arguments.stMenu.security.serial,hidden:arguments.adminType NEQ "server" or server.ColdFusion.ProductLevel NEQ "enterprise",display:true)
			)
		)/*,
		struct(action:"documentation",label:arguments.stMenu.documentation.label,
			children:array(
				struct(action:"tagRef",label:arguments.stMenu.documentation.tagRef),
				struct(action:"funcRef",label:arguments.stMenu.documentation.funcRef),
				struct(action:"objRef",label:arguments.stMenu.documentation.objRef)
			)
		)*/
	)>
    <cfreturn MenuStruct>
</cffunction>
