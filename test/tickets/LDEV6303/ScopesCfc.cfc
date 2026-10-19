component accessors=true {
	static {
		static.PREFIX = "static-prefix";
	}
	property name="fromStatic"   type="string"  default="#static.PREFIX#";
	property name="fromServer"   type="string"  default="#server.lucee.version#";
	property name="fromOwnUdf"   type="string"  default="#makeValue()#";
	property name="mixedString"  type="string"  default="pre-#repeatString( 'x', 2 )#-post";
	property name="arithmetic"   type="numeric" default="#1 + 2#";
	property name="nested"       type="any"     default='#{ a: { b: [ 1, 2, { c: "d" } ] } }#';
	property name="unquotedExpr" type="any"     default=#repeatString( 'u', 2 )#;

	private string function makeValue() {
		return "from-udf";
	}
}
