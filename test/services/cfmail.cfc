/* with lucee 7.1, mail was moved to an extension, 
	test here when the tag exists, to avoid compile error */
component {
	static function testMail(smtp){
		cfmail(from="testsuite@lucee.org",
					to="testsuite@lucee.org",
					subject="service test",
					async=false,
					server=smtp.SERVER,
					port=smtp.PORT_INSECURE,
					username=smtp.USERNAME,
					password=smtp.PASSWORD) {
			echo("test suite service test email");
		}
	}
}