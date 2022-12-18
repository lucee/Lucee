component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	public void function testGetTickCount() {
		var tick = getTickCount();
		expect (tick ).toBeNumeric();
		sleep(5);
		var tick2 = getTickCount();
		expect (tick2 ).toBeGT( tick );
	}

}
