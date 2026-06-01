// Expression-form, mutable-expression, and literal-form property defaults.
component accessors="true" {

	property name="uuidDef"      type="string" default="#createUUID()#";
	property name="nowDef"       type="any"    default="#now()#";
	property name="literalDef"   type="string" default="construction-literal";
	property name="nowStructDef" type="any"    default='#{"ts":now(),"n":1}#';

}
