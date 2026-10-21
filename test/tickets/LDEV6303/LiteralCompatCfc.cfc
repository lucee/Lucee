component accessors=true {
	property name="plain"          type="string"  default="hello";
	property name="looksLikeCall"  type="string"  default="now()";
	property name="escapedHash"    type="string"  default="##now()##";
	property name="foldedLiteral"  type="string"  default="#'folded'#";
	property name="emptyString"    type="string"  default="";
	property name="quotedNumber"   type="numeric" default="5";
	property name="bareNumber"     type="numeric" default=7;
	property name="bareBoolean"    type="boolean" default=true;
	property name="noDefault"      type="any";
}
