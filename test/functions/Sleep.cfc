component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testSleep() {
		var tick = getTickCount();
		sleep( 10 );
		var tick2 = getTickCount();
		expect ( tick2-tick ).toBeGTE( 10 );
	}

}
