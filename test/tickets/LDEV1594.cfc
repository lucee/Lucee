component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1594", function() {
			it( title='checking LSParseDateTime() with attribute format', body=function( currentSpec ) {
				try {
					var ts1 = LSParseDateTime("27 november 2017","en","dd mmmmm yyyy");
				} catch ( any e){
					ts1 = e.message;
				}
				assertEquals("{ts '2017-11-27 00:00:00'}", ts1);
				assertEquals("{ts '2017-11-27 00:00:00'}", LSParseDateTime("27 november 2017","en","dd MMMMM yyyy"));
				assertEquals("{ts '2017-11-27 00:00:00'}", LSParseDateTime("27 november 2017","en","dd MMMMM YYYY"));
				assertEquals("{ts '2017-11-27 07:02:33'}", LsParseDateTime("11/27/17 7:02:33",'en','MM/dd/yy h:mm:ss'));
				assertEquals("{ts '2017-11-27 07:02:33'}", LsParseDateTime("11/27/17 7:02:33",'en','MM/dd/YY h:mm:ss'));
			});
		});
	}
}