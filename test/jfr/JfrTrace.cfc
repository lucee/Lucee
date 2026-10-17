component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function testCftraceWithJfr() labels="jfr" {
		expect( function() {
			cftrace( type="Information", category="test", text="Test trace with JFR", jfr=true );
		} ).notToThrow();
	}

	function testCftraceWithJfrAndVariable() labels="jfr" {
		expect( function() {
			var testVar = { name: "test", count: 42 };
			cftrace( type="Information", category="test", text="Trace with variable", var="testVar", jfr=true );
		} ).notToThrow();
	}

	function testCftraceWithoutJfr() labels="jfr" {
		// Should work normally without jfr attribute
		expect( function() {
			cftrace( type="Information", category="test", text="Normal trace" );
		} ).notToThrow();
	}
}
