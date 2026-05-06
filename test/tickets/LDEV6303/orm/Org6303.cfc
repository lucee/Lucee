component persistent="true" table="LDEV6303" {
	property name="id"        type="string" fieldtype="id" ormtype="string";
	property name="literalDef" type="string" default="literal-default";
	property name="exprDef"    type="string" default="#repeatString( 'x', 5 )#";
}
