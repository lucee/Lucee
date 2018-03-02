<cfcomponent displayName="testQoQ">
	<cffunction name="getAsset" access="public" returnType="any" output="true">
		<cfscript>
			var local={};
			local.assets=getAssets();
		</cfscript>
		<cfquery name="local.q_assets" dbtype="query">
			select 	*
			from 	[local].assets
			group 	by asset_id, asset_name
			ORDER 	BY asset_name
		</cfquery>
		<cfreturn local.q_assets>
	</cffunction>

	<cffunction name="getAssets" returntype="query" access="private" output="false">
		<cfset var q_assets=""/>
		<cfquery name="q_assets">
			SELECT 	*
			FROM 	test_asset
			group 	by asset_id, asset_name
			ORDER 	BY asset_name
		</cfquery>
		<cfreturn q_assets>
	</cffunction>
</cfcomponent>

