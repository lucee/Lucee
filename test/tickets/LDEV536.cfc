component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults , testBox ) {
		describe( title='LDEV-536' , body=function(){
			it( title='test parseDateTime ' , body=function() {
				
				var src = 'Tue, 20 Jan 2015 00:00:00 Z';
				var date = parseDateTime( date:src,timezone:"UTC");
				
				var format =dateTimeFormat(date:date,timezone:"UTC");
				expect(format).toBe("20-Jan-2015 00:00:00");

			});

			it(title = "Unable to handle RFC822 datetime string with Z for timezone", body = function( currentSpec ) {
				var dt = 'Tue, 11 Dec 2018 00:00:00 Z';
				expect(isdate( dt )).toBe('true'); 
				expect(parsedatetime(dt)).toBe(dateTimeFormat(dt,'yyyy-mm-dd hh:nn:ss'));
			});

			it(title = "Unable to handle RFC822 datetime string with other timezone", body = function( currentSpec ) {
				var foo = 'Tue, 11 Dec 2018 00:00:00 GMT';
				expect(isdate( foo )).toBe('true'); 
				expect(parsedatetime(foo)).toBe(dateTimeFormat(foo,'yyyy-mm-dd hh:nn:ss'));
			});
		});
	}

}