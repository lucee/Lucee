component accessors=true extends="OverrideParentCfc" {
	property name="exprToLiteral" type="any" default="child-literal";
	property name="literalToExpr" type="any" default="#repeatString( 'c', 2 )#";
	property name="exprToExpr"    type="any" default="#repeatString( 'c', 3 )#";
}
