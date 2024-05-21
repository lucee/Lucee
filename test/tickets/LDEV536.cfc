component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults , testBox ) {
		describe( title='LDEV-536' , body=function(){
			it( title='test parseDateTime ' , body=function() {
				
				var src = 'Tue, 20 Jan 2015 00:00:00 Z';
				var date = parseDateTime( date:src,timezone:"UTC");
				
				var format =dateTimeFormat(date:date,timezone:"UTC");
				expect(format).toBe("20-Jan-2015 00:00:00");

			});
		});
	}

}