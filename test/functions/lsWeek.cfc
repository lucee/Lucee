component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for lsWeek()", body=function() {
			it(title="checking lsWeek() with locale argument", body = function( currentSpec ) {
				var date = createDateTime(2022,01,17,12,0,0,0,"UTC"); 
				
				if(getJavaVersion()<=8){
					// in Arabic (Yemen) Saturday is the first day of the week
					expect(lsWeek(date=date, locale="Arabic (Yemen)")).tobe(3);
					// in Catalan Monday is the first day of the week
					expect(lsWeek(date=date, locale="Catalan")).tobe(3);
				}else{
					// after Java 8 in Arabic (Yemen) Sunday is the first day of the week
					expect(lsWeek(date=date, locale="Arabic (Yemen)")).tobe(4);
					// after Java 8 in Catalan Sunday is the first day of the week
					expect(lsWeek(date=date, locale="Catalan")).tobe(4);
					// Testing Iraq because it still has Saturday as the first day of the week after Java 8
					expect(lsWeek(date =date, locale="ar_IQ")).toBe(3);
				}
				// in English (Canada) Sunday is the first day of the week
				expect(lsWeek(date=date, locale="English (Canada)")).tobe(4);
			});
			it(title="checking lsWeek() with locale and timezone argument", body = function( currentSpec ) {
				var date = createDateTime(2022,01,17,12,0,0,0,"UTC"); 
				// in CH Monday is the first day of the week
				expect(lsWeek(date, "DE_CH", "Europe/Zurich")).toBe(3);
				// in Bagdad Monday is the third day of the week
				expect(lsWeek(date, "ar_IQ", "Asia/Baghdad")).toBe(3);
				// in the US Monday is the second day of the week
				expect(lsWeek(date, "EN_US", "America/Los_Angeles")).toBe(4);
			});
		});
	}
	
	private function getJavaVersion() {
        var raw=server.java.version;
        var arr=listToArray(raw,'.');
        if(arr[1]==1) // version 1-9
            return arr[2];
        return arr[1];
    }
	
}
