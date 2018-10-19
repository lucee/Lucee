component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateTime()", body=function() {
			it(title="checking CreateTime() function", body = function( currentSpec ) {
				assertEquals("{t '12:11:00'}x","#createTime(12,11)#x");
				assertEquals("{t '12:00:00'}x","#createTime(12)#x");
				assertEquals("{t '12:00:33'}x","#createTime(hour:12,second:33)#x");
				assertEquals("{t '12:11:10'}x","#createTime(12,11,10)#x");
			});
		});
	}
}