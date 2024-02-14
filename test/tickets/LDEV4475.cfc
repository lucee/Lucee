component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, textbox ) {
		describe(title="Testcase for LDEV-4475", body=function() {
			it(title="checking left()", body=function( currentSpec ) {
				expect ( function(){
					var x = ".";
					(left(x, 1) eq "");
				}).notToThrow();

				expect ( function(){
					(left(".", 1) eq "");
				}).notToThrow();
			});
		});
	}
}
