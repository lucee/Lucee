component hint="main component with sub components" {

	function init() {
		return this;
	}

	function mainFunction() {
		return "main";
	}

}

component name="First" {

	function firstFunction() {
		return "first";
	}

}

component name="Second" {

	function secondFunction() {
		return "second";
	}

}
