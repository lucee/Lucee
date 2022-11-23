component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for lsWeek()", body=function() {
			it(title="checking lsWeek() with locale argument", body = function( currentSpec ) {
				// in Arabic (Yemen) Saturday is the first day of the week
				expect(lsWeek(date="{ts '2022-01-17 0:0:0'}", locale="Arabic (Yemen)")).tobe(3);
				// in Catalan monday is the first day of the week
				expect(lsWeek(date="{ts '2022-01-17 0:0:0'}", locale="Catalan")).tobe(3);
				// in English (Canada) sunday is the first day of the week
				expect(lsWeek(date="{ts '2022-01-17 0:0:0'}", locale="English (Canada)")).tobe(4);
			});
			it(title="checking lsWeek() with locale and timezone argument", body = function( currentSpec ) {
				var date = createDateTime(2022,01,17,12,0,0,0,"UTC"); 
				// in CH Monday is the first day of the week
				expect(lsWeek(date,"DE_CH","Europe/Zurich")).toBe(3);
				// in Bagdad Monday is the thrird day of the week
				expect(lsWeek(date,"ar_IQ","Asia/Baghdad")).toBe(3);
				// in the US Monday is the second day of the week
				expect(lsWeek(date,"EN_US","America/Los_Angeles")).toBe(4);
			});
		});
	}
}