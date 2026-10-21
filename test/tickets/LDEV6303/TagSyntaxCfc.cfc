<cfcomponent accessors="true">
	<cfproperty name="tagLiteral" type="string" default="tag-literal">
	<cfproperty name="tagExpr" type="any" default="#repeatString( 't', 3 )#">
	<cfproperty name="tagNow" type="any" default="#now()#">
	<cfproperty name="tagStruct" type="any" default="#{ a: 1 }#">
	<cfproperty name="tagLooksLikeExpr" type="string" default="now()">
	<cfproperty name="tagEscapedHash" type="string" default="##now()##">
</cfcomponent>
