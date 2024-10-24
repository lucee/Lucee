component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-5120", function() {

			it("checking loading the same class at the same time by multiple threads with the dynamic classloader", function( currentSpec ){
				
				var names=[];
				loop from=1 to=100 index="local.i" {
					var name="dyninvoc#i#";
					arrayAppend(names, name);
					thread name=name {
						// create the dynamicwrapper for the class and use it, this class get nowhere else used, so on a fresh server that should trigger it for the first time
						thread.message=new java.util.prefs.BackingStoreException("susi").getMessage();
					}
				}

				thread action="join" name=arrayToList(names);
				loop array=names item="local.name" {
					// simply throw any exception hapening inside the thread
					if(!isNull(cfthread[name].error)) throw cfthread[name].error;
				}
			});

			it("checking loading the same class at the same time by multiple threads with the dynamic classloader with member function call", function( currentSpec ){
				
				var names=[];
				loop from=1 to=100 index="local.i" {
					var name="dyninvoc2#i#";
					arrayAppend(names, name);
					thread name=name {
						// create the dynamicwrapper for the class and use it, this class get nowhere else used, so on a fresh server that should trigger it for the first time
						var arr=[1,2];
						var x=arr.len();
					}
				}

				thread action="join" name=arrayToList(names);
				loop array=names item="local.name" {
					// simply throw any exception hapening inside the thread
					if(!isNull(cfthread[name].error)) throw cfthread[name].error;
				}
			});
		});
	}
}