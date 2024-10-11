component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function run( testResults , testBox ) {
		describe( title='LDEV-5103', body=function(){

			it( title='concurrency issue in Clazz.getArgumentClasses', body=function() {
				var names=[];
				loop from=1 to=100 index="local.i" {
					var name="t5103-#i#";
					arrayAppend(names, name);
					thread name="#name#" {
						variables.dateParser = createObject("java", "java.text.SimpleDateFormat").init("yyyy-MM-dd'T'HH:mm:ssX");
					}
				}

				thread action="join" name=names.toList();
				loop array=names item="name" {
					if(structKeyExists(cfthread[name],"ERROR")) throw cfthread[name].error;
				}
			});

		});
	}

}