component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public function setUp() localmode="true"{
		
	}

	public void function test() localmode="true"{
		clearTimezone();
		var orig=getTimeZone();
		var javatz = CreateObject("java", "java.util.TimeZone");
	    var tz_list = javatz.getAvailableIDs(); 
	    for( i=1; i LTE arrayLen(tz_list); i++) {
	       setTimeZone(tz_list[i]);
	    }
	    clearTimezone();

		assertEquals(getTimeZone(),orig);
	}

} 