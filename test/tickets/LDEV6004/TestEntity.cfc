component accessors="true" {

	// String
	property name="stringNoDefault" type="string";
	property name="stringWithDefault" type="string" default="foo";
	property name="stringEmptyDefault" type="string" default="";

	// Numeric
	property name="numericNoDefault" type="numeric";
	property name="numericWithDefault" type="numeric" default="42";

	// Boolean
	property name="booleanNoDefault" type="boolean";
	property name="booleanWithDefault" type="boolean" default="true";

	// Date
	property name="dateNoDefault" type="date";

	// Array
	property name="arrayNoDefault" type="array";

	// Struct
	property name="structNoDefault" type="struct";

	// Any
	property name="anyNoDefault" type="any";
	property name="anyWithDefault" type="any" default="bar";

}
