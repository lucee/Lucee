component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults , testBox ) {
		describe( title='LDEV-764' , body=function(){
			it( title='test parseDateTime with Timezone' , body=function() {
				var src = "Mon Feb 29 2016 00:00:00 GMT+0530 (IST)";
				var date = parseDateTime( date:src,timezone:"IST");
				var format = dateTimeFormat(date,"E MMM dd YYYY HH:nn:ss 'GMT'Z (zz)","IST");
				expect(format).toBe(src);
			});
			it( title='test parseDateTime without Timezone' , body=function() {
				var src = "Mon Feb 29 2016 00:00:00 GMT+0530 (IST)";
				var date = parseDateTime( date:src);
				var format = dateTimeFormat(date,"E MMM dd YYYY HH:nn:ss 'GMT'Z (zz)","IST");
				expect(format).toBe(src);
			});
			it( title='test DateAdd without Timezone' , body=function() {
				var src = "Mon Feb 29 2016 00:00:00 GMT+0530 (IST)";
				var date = DateAdd("s", 0, src);
				var format = dateTimeFormat(date,"E MMM dd YYYY HH:nn:ss 'GMT'Z (zz)","IST");
				expect(format).toBe(src);
			});
		});
	}

}