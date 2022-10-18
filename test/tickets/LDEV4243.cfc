component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe("Testcase for LDEV-4243", function() {

			it(title = "Checking hash() without using numIterations argument", body = function( currentSpec ) {
				assertEquals("E269BDCA8E94A1B763D903101516DD99", hash("i love lucee", 'MD5', 'UTF-8'));
				assertEquals("DFF444B70AF4EE99E58643A05397C100184F9E51", hash("i love lucee", 'SHA', 'UTF-8'));
				assertEquals("CC96B66778C27ED0DAB001CC59B1A24B737CE2A2CEB05ECEF557EA9A854D15DB", hash("i love lucee", 'SHA-256', 'UTF-8'));
				assertEquals("90DF5200C57F478BE107B3A0291AF62859392168AE7575F3AE417D3FF05CD6001BA4CB481CAE4E76B97BB6DEC58B1BB5", hash("i love lucee", 'SHA-384', 'UTF-8'));
				assertEquals("ACC15D1E72CB56534DC2CF8ED3B71349C07620202ED133D56BBC00732C7C2F7B8C91CA5D9F99F54E26618675378EE5F3272F0AAFCBEA05E889795DA313988BFB", hash("i love lucee", 'SHA-512', 'UTF-8'));
			});	

			it(title = "Checking hash() with using numIterations argument", body = function( currentSpec ) {
				assertEquals("E269BDCA8E94A1B763D903101516DD99", hash("i love lucee", 'MD5', 'UTF-8', 0 ));
				assertEquals("3AB78DB36EB089E1059A5ED2A0393EA159E69655", hash("i love lucee", 'SHA', 'UTF-8', 1 ));
				assertEquals("DFF444B70AF4EE99E58643A05397C100184F9E51", hash("i love lucee", 'SHA', 'UTF-8', -1 ));
				assertEquals("DFF444B70AF4EE99E58643A05397C100184F9E51", hash("i love lucee", 'SHA', 'UTF-8', 0 ));
				assertEquals("2E65FC19625FA61291EB4918AF7D75B1", hash("i love lucee", 'MD5', 'UTF-8', 10 ));
				assertEquals("85900B2863113DB00161926E635DA4655E9C3A7C", hash("i love lucee", 'SHA','UTF-8', 10 ));
				assertEquals("85E2F031FC9792EBA99A315099C0EAE7679C09BE60DA0E1FF12495E77542DA8A", hash("i love lucee", 'SHA-256', 'UTF-8', 10 ));
				assertEquals("9A2D58827ABF80D24E42E0CEBD24886D0C0BF788CFDC69FB2C9F62D703A8049019DD1BAB29EACE6D982EF48D65E66991", hash("i love lucee", 'SHA-384', 'UTF-8', 10 ));
				assertEquals("CA739F6B982E9D5E2111AB47655635A6B09ED38C7CDB7D96401CC5E3DF34573F71C107BDAAE4D997C7AC1E8F519CAF0700A6B6AA56CB197D0FC22D9930114E8E", hash("i love lucee", 'SHA-512', 'UTF-8', 10 ));
			});		
		});	
	}
}
