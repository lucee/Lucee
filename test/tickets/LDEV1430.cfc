component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml"{
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1430", function() {
			it(title="checking XMLParse()", body = function( currentSpec ) {
				var strXml = '<?xml version="1.0" encoding="UTF-8"?><template><security><group/><user/></security></template>';
				var strXML2='<?xml version="1.0" encoding="UTF-8"?>
<users><user name="abra"><password/><name>abra</name><password/><emailaddress>mic@susi.it</emailaddress></user><user name="hansueli"><password>96e79218965eb72c92a549dd5a330112</password><name>hansueli</name><password/><emailaddress>hansueli@sss.ch</emailaddress></user><user name="111111"><password/><name>sdasdasd</name><password/><emailaddress>sdfsad@asdsd.ch</emailaddress></user><user name="jedimaster"><password/><name>Raymond Camden</name><password/><emailaddress>ray@camdenfamily.com
</emailaddress></user><user name="neomouse"><password/><name>Michael</name><password/><emailaddress>neo@susi.it</emailaddress></user><user name="sdasdasdasd"><password>96e79218965eb72c92a549dd5a330112</password><name>sdasdasd</name><password/><emailaddress>sdsadsdas@sdasfd.ch</emailaddress></user><user name="111111yyy"><password/><name>sdsdsd</name><password/><emailaddress>sdsds@sds.ch</emailaddress></user><user name="admin"><password/><name>Admin</name><password/><emailaddress>admin@localhost.org</emailaddress></user><user name="michi"><password/><name>Michael Mueller</name><password/><emailaddress>hallo@susi.it</emailaddress></user></users>';
				var xmL = xmlParse(strXml, true);
				var xmL2 = xmlParse(strXml2, true);
				assertEquals(1, arrayLen(xml.xmlchildren));
				assertEquals(1, arrayLen(xml2.xmlchildren));
			});
		});
	}
}