<!--- Tag-syntax cfproperty parity with the script-syntax sibling. --->
<cfcomponent accessors="true">

	<cfproperty name="uuidDef"      type="string" default="#createUUID()#">
	<cfproperty name="nowDef"       type="any"    default="#now()#">
	<cfproperty name="literalDef"   type="string" default="construction-literal">
	<cfproperty name="nowStructDef" type="any"    default='#{"ts":now(),"n":1}#'>

</cfcomponent>
