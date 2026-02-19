<cfcomponent>
	<!--- Tag style - no type attribute --->
	<cfproperty name="TagMixins" inject="id:Plugins">
	<!--- Tag style - WITH explicit type --->
	<cfproperty name="TagWithType" type="string" inject="id:Config">
	<!--- Tag style - WITH explicit type="any" (should be preserved!) --->
	<cfproperty name="TagExplicitAny" type="any" inject="id:Service">
</cfcomponent>
