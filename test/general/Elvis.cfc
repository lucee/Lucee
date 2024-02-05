component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}

	function afterAll(){
		
	}

	function run( testResults , testBox ) {
		describe( "test suite for the elvis operator", function() {

			it(title="test not existing variable", body=function() {
				expect(not_.existing_.var_?:'NotExisting').toBe("NotExisting");
				expect(not_existing_var?:"NotExisting").toBe("NotExisting");
			});

			it(title="test existing variable", body=function() {
				var notexa.b.c.d="Existing";
				expect(notexa.b.c.d?:'NotExisting').toBe("Existing");
				expect(notexa?:"NotExisting").toBeTypeOf("struct");
			});

			it(title="test number default", body=function() {
				expect(not_.existing_.var_?:0).toBe(0);
				expect(not_existing_var?:0).toBe(0);
				expect(not_existing_var?:0).toBeTypeOf("numeric");
			});

			it(title="test boolean default", body=function() {
				expect(not_.existing_.var_?:true).toBe(true);
				expect(not_existing_var?:true).toBe(true);
				expect(not_existing_var?:true).toBeTypeOf("boolean");
			});

			it(title="test boolean default", body=function() {
				var d=now();
				expect(not_.existing_.var_?:d).toBe(d);
				expect(not_existing_var?:d).toBe(d);
				expect(not_existing_var?:d).toBeTypeOf("datetime");
			});

			it(title="test UDF default", body=function() {
				expect(not_.existing_.var_?:_test()).toBe(123);
			});

			it(title="test use result as a math operant", body=function() {
				expect( (not_.existing_.var_?:1)+1  ).toBe(2);
			});

			it(title="testing a load test with multithreading", body=function(){
				
				var max=20; //how many concurrent threads to run

				var names=[];
				var server.a.b.c={};

				loop from=1 to=max index="local.i" {
					var name="testelvis"&i;
					arrayAppend(names, name);
					// threads that do the elvis on a variable that is set and removed all the time
					thread name=name  {
						loop times=100 {
							thread.test=server.a.b.c.d?:nullValue(); 
						}
					}
					// threads that set and remove the variable all the time
					thread name=name&"x" sleeptime=i-1 {
						loop times=20 {
							server.a.b.c.d="";
							structDelete(server.a.b.c, "d",false);
							if(sleeptime>0) sleep(sleeptime); 
						}
					}
				}
				// wait that all test threads do finish (we dn't care about the threads set/unset the variable)
				thread action="join" name=arrayToList(names);
				
				// check if there is a thread that did not complete (means failed)
				loop array=names item="local.name" {
					if(cfthread[name].STATUS!="COMPLETED" && !isNull(cfthread[name].ERROR)) {
						throw cfthread[name].ERROR; // rethrow the exception from inside the thread (lot of fun)
					}
				}
			});

		});
	}



	private function testElvis(){		
		return "Existing";
	}

	private function _test(){
		return 123;
	}
	private function rtnNull(){	
	}
}
