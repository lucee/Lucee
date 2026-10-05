component accessors=true {
	property name="literalDef"    type="string" default="hello";
	property name="expressionDef" type="any"    default="#repeatString( 'x', 5 )#";
	property name="nowDef"        type="any"    default="#now()#";
	property name="nowStructDef"  type="any"    default='#{"ts":now(),"n":1}#';
	property name="uuidDef"       type="string" default="#createUUID()#";
	property name="freshArrayDef" type="any"    default='#[]#';
}
