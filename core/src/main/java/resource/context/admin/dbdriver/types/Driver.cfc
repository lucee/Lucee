<cfcomponent output="no">
	
	<cfset this.TYPE_HIDDEN=0>
	<cfset this.TYPE_FREE=1>
	<cfset this.TYPE_REQUIRED=2>
	
	<cfset this.type=struct(
		host:this.TYPE_FREE,
		database:this.TYPE_REQUIRED,
		port:this.TYPE_HIDDEN,
		username:this.TYPE_FREE,
		password:this.TYPE_FREE
	)>
	<cfset this.value=struct(
		host:"",
		database:"",
		port:"",
		username:"",
		password:"",
		connectionLimit:"",
		ConnectionTimeout:"1",
		blob:false,
		clob:false,
		allowed_select:true,
		allowed_insert:true,
		allowed_update:true,
		allowed_delete:true,
		allowed_alter:true,
		allowed_revoke:true,
		allowed_drop:true,
		allowed_grant:true,
		allowed_create:true
	)>
	
	
	<cffunction name="field" returntype="component" access="private" output="no">
		<cfargument name="displayName" required="true" type="string">
		<cfargument name="name" required="true" type="string">
		<cfargument name="defaultValue" required="false" type="string" default="">
		<cfargument name="required" required="false" type="boolean" default="no">
		<cfargument name="description" required="false" type="string" default="">
		<cfargument name="type" required="false" type="string" default="text">
		<cfargument name="defaultValueIndex" required="false" type="number" default="1">
		<cfreturn createObject("component","Field").init(arguments.displayName,arguments.name,arguments.defaultValue,arguments.required,arguments.description,arguments.type,arguments.defaultValueIndex)>
	</cffunction>
	
	
	<cffunction name="equals" returntype="boolean" output="false"
		hint="return if String class match this">
		
		<cfargument name="className"	required="true">
		<cfargument name="dsn"			required="true">
		
		<cfreturn this.className EQ arguments.className and this.dsn EQ arguments.dsn>
	</cffunction>
	
	<cffunction name="getType" returntype="numeric" output="no">
		<cfargument name="key" required="true" type="string">
		<cfreturn this.type[arguments.key]>
	</cffunction>
	
	<cffunction name="getValue" returntype="string" output="no">
		<cfargument name="key" required="true" type="string">
		<cfreturn this.value[key]>
	</cffunction>
	
	<cffunction name="getClass" returntype="string" output="no" 
		hint="return driver Java Class">
		
		<cfreturn this.className>
	</cffunction>
	
	<cffunction name="getDSN" returntype="string" output="no" 
		hint="return DSN">
		
		<cfreturn this.dsn>
	</cffunction>
	
	<cffunction name="onBeforeUpdate" returntype="void" output="false">
	</cffunction>
	
	<cffunction name="onBeforeError" returntype="void" output="no">
		<cfargument name="cfcatch" required="true" type="struct">
	</cffunction>
	
	<cffunction name="init" returntype="void" output="no">
		<cfargument name="data" required="yes" type="struct">
		
	</cffunction>
	

</cfcomponent>