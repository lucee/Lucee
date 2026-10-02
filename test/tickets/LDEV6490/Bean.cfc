component accessors="true" {
	property name="name" type="string";

	function hello() {
		return "hello " & getName();
	}
}
