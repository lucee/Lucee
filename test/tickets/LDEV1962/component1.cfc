component accessors=true {
	property name="message";
	
	function init() {
		variables.message = 'This message from component1.cfc!';
	}

	function testFun() {
		return getMessage();
	}
	
}