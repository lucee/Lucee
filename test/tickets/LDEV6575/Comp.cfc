component accessors=true {
	function readSubName() {
		var s = new Comp$Sub();
		s.setName( "lucee" );
		return s.name;
	}
}
component name="Sub" accessors=true {
	property name="name" type="string";
	function getName() {
		return "getter:" & variables.name;
	}
}
