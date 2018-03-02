component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1302", body=function(){
			it(title="test", body=function() {
				sTarget = isEmpty(url.target1302 ?: '') ? '/##/index.cfm?&uebergabe=#url.uebergabe1302 ?: 0#' : url.target1302;
			});
			
			// 


		});
	}
}