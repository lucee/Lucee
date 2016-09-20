<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function test(){

		var string = "This is a_test of application/x-www-form-urlencoded MIME format ~:)";

		var str1 = urlEncode(string);
		var str2 = urlEncodedFormat(string);

		assertTrue(str1 CT "_");
		assertTrue(str2 NCT "_");

		assertEquals(URLDecode(str1), URLDecode(str2));
	}

}
</cfscript>