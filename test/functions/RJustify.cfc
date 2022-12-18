component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testRJustify(){
		expect ( "abc".rJustify( 10 ) ).toBe("       abc");
		expect ( RJustify( "abc", 10 ) ).toBe("       abc");
	}

}
