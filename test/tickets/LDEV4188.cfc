component extends="org.lucee.cfml.test.LuceeTestCase" skip="false" labels="qoq" {
	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-4188", body=function() {
			it(title="Checking BigDecimal serialize", body = function( currentSpec ) {
				var bd=CreateObject("java","java.math.BigDecimal").valueOf(123.0);
				expect(serialize({bd:bd})).tobe('{"BD":123}');
			});
			it(title="Checking BigDecimal serializeJson", body = function( currentSpec ) {
				var bd=CreateObject("java","java.math.BigDecimal").valueOf(123.0);
				expect(serializeJson({bd:bd})).tobe('{"BD":123}');
			});
			it(title="Checking double serialize", body = function( currentSpec ) {
				var d=123.0;
				expect(serialize({d:d})).tobe('{"D":123}');
			});
			it(title="Checking double serializeJson", body = function( currentSpec ) {
				var d=123.0;
				expect(serializeJson({d:d})).tobe('{"D":123}');
			});
		});
	}
}