component extends="org.lucee.cfml.test.LuceeTestCase"  {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV4766", function() {
			it( title='test concurrency for lsParseCurrency', body=function( currentSpec ) {
				var names=[];
				loop from=1 to=10 index="local.idx" {
					var name="tlsPC"&idx;
					arrayAppend(names, name);
					thread name=name {
						for (var i = 0; i < 10000; i++) {
							thread.result=lsParseCurrency( "42.42" );
						}
					}
				}
				thread action="join" name=arrayToList(names);
				

				loop array=names item="name" {
					if(cfthread[name].STATUS!="completed") throw cfthread[name].ERROR;
				}
			});
		});
	}
}
