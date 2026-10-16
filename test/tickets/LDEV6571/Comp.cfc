component {
	function init() {
		variables.s = new Comp$Sub();
		return this;
	}
	function getSub() {
		return variables.s;
	}
	Sub function returnShort() {
		return variables.s;
	}
	function acceptShort( required Sub s ) {
		return true;
	}
	function acceptParent( required Comp c ) {
		return true;
	}
	Sub function returnWrong() {
		return this;
	}
}
component name="Sub" {
	public Sub function init() {
		return this;
	}
}
