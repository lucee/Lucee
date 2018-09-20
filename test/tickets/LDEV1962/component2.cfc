component {
	
	variables.message = 'This message from component2.cfc!';

	function testFun() {
		return getMessage();
	}
	function testFunx() {
		return getMessagex();
	}

	function testFun2() {
		return getMessage2();
	}

	function testFun2x() {
		return getMessage2x();
	}
	
}