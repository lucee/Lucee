component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}

	function afterAll(){
		structDelete(request, "testfunctionlistenerecho");
		
	}

	function mySuccess() {
		return "Susi Sorglos";
	}
	function myError() {
		throw "Upsi dupsi!"
	}
	function logAndFail(name,value) {
		request.testFunctionListenerEcho[name]=value;
		throw "Upsi dupsi!"
	}

	function run( testResults , testBox ) {
		describe( "test suite for function listeners", function() {


			it(title="simple version", body=function() {
				var t=mySuccess():function(result,error) {
					variables.testFunctionListenerV=result;
					thread.testFunctionListenerV=result;
				};
				// wait for the thread to finish
				sleep(100);
				expect(cfthread[t].testFunctionListenerV?:"undefined1").toBe("Susi Sorglos");
				// TODO expect(variables.testFunctionListenerV?:"undefined2").toBe("Susi Sorglos");
			});

			it(title="listening on a UDF that does NOT fail (joining the thread)", body=function() {
				var threadName=mySuccess():function(result,error) {
					thread.result=result;
				};
				// wait for the thread to finish
				threadJoin(threadName);

				expect(cfthread[threadName].result).toBe("Susi Sorglos");
			});

			it(title="listening on a UDF that DOES fail (joining the thread)", body=function() {
				var threadName=myError():function(result,error) {
					thread.result=error.message;
				};
				// wait for the thread to finish
				threadJoin(threadName);

				expect(cfthread[threadName].result).toBe("Upsi dupsi!");
			});

			it(title="listening on a BIF that does NOT fail (joining the thread)", body=function() {
				var threadName=arrayLen([1,2,3]):function(result,error) {
					thread.result=result;
				};
				// wait for the thread to finish
				threadJoin(threadName);

				expect(cfthread[threadName].result).toBe(3);
			});
			
			it(title="listening on a UDF chain that does NOT fail (joining the thread);undefined",  body=function()  {
				var a.b.c.d=mySuccess;
				var threadName=a.b.c.d():function(result,error) {
					thread.result=result?:error;
				};
				// wait for the thread to finish
				threadJoin(threadName);
				expect(cfthread[threadName].result).toBe("Susi Sorglos");
			});
			
			it(title="listening on a UDF chain that does NOT fail (joining the thread);local",  body=function()  {
				local.a.b.c.d=mySuccess;
				var threadName=local.a.b.c.d():function(result,error) {
					thread.result=result?:error;
				};
				// wait for the thread to finish
				threadJoin(threadName);
				expect(cfthread[threadName].result).toBe("Susi Sorglos");
			});

			it(title="listening on a UDF chain that does NOT fail (joining the thread);variables",  body=function()  {
				variables.a.b.c.d=mySuccess;
				var threadName=variables.a.b.c.d():function(result,error) {
					thread.result=result?:error;
				};
				// wait for the thread to finish
				threadJoin(threadName);
				expect(cfthread[threadName].result).toBe("Susi Sorglos");
			});

			it(title="listening on a UDF chain that does NOT fail (joining the thread);arguments",  body=function()  {
				arguments.a.b.c.d=mySuccess;
				var threadName=arguments.a.b.c.d():function(result,error) {
					thread.result=result?:error;
				};
				// wait for the thread to finish
				threadJoin(threadName);
				expect(cfthread[threadName].result).toBe("Susi Sorglos");
			});

			it(title="listening on a component (no package) instantiation that does NOT fail (joining the thread)", body=function() {
				var threadName=new Query():function(result,error) {
					thread.result=result;
				};

				// wait for the thread to finish
				threadJoin(threadName);

				expect(getMetadata(cfthread[threadName].result).fullname).toBe("org.lucee.cfml.Query");
			});

			it(title="listening on a component (with package) instantiation that does NOT fail (joining the thread)", body=function() {
				var threadName=new org.lucee.cfml.Query():function(result,error) {
					thread.result=result;
				};

				// wait for the thread to finish
				threadJoin(threadName);

				expect(getMetadata(cfthread[threadName].result).fullname).toBe("org.lucee.cfml.Query");
			});

			it(title="listening on a static component function (no package) that does NOT fail (joining the thread)", body=function() {
				var threadName=Query::new(["columnName"]):function(result,error) {
					thread.result=result;
				};

				// wait for the thread to finish
				threadJoin(threadName);

				expect(cfthread[threadName].result.columnlist).toBe("COLUMNNAME");
			});

			it(title="listening on a static component function (with package) that does NOT fail (joining the thread)", body=function() {
				var threadName=org.lucee.cfml.Query::new(["columnName"]):function(result,error) {
					thread.result=result;
				};

				// wait for the thread to finish
				threadJoin(threadName);

				expect(cfthread[threadName].result.columnlist).toBe("COLUMNNAME");
			});

			it(title="listening on a UDF (joining the thread), send data to a function collection; test success", body=function() {
				var coll1={
					onSuccess:function(result) {
						thread.success=result;
					}
					,onFail:function(result,error) {
						thread.fail=error.message;
					}
				};
				
				var threadName1=mySuccess():coll1;
			
				// wait for the thread to finish
				threadJoin(threadName1);
				expect(cfthread[threadName1].success).toBe("Susi Sorglos");
			});

			it(title="listening on a UDF (joining the thread), send data to a function collection; test fail", body=function() {
				var threadName2=myError():{
					onSuccess:function(result) {
						thread.success=result;
					}
					,onFail:function(result,error) {
						thread.fail=error.message;
					}
				};
			
				// wait for the thread to finish
				threadJoin(threadName2);

				expect(cfthread[threadName2].fail).toBe("Upsi dupsi!");
			});

			it(title="listening on a UDF (joining the thread), send data to a component; test success", body=function() {
				var cfc=createObject("component","functionListener/Test.cfc");
				var threadName1=mySuccess():cfc;
				
				// wait for the thread to finish
				threadJoin(threadName1);
				expect(cfthread[threadName1].success).toBe("Susi Sorglos");
			});

			it(title="listening on a UDF (joining the thread), send data to a component; test fail", body=function() {
				var cfc=createObject("component","functionListener/Test.cfc");
				var threadName2=myError():cfc;
			
				// wait for the thread to finish
				threadJoin(threadName2);

				expect(cfthread[threadName2].fail).toBe("Upsi dupsi!");
			});

			it(title="async execution without a listener; null", body=function() {
				// passing null
				var threadName1=logAndFail("testNull","Peter Lustig"):nullValue();
				
				// wait for the thread to finish
				threadJoin(threadName1);
				expect(request.testFunctionListenerEcho.testNull).toBe("Peter Lustig");
			});
			it(title="async execution without a listener;switch", body=function() {
				// passing empty struct
				var threadName2=logAndFail("testStruct","Ruedi Zraggen"):{};

				// wait for the thread to finish
				threadJoin(threadName2);
				expect(request.testFunctionListenerEcho.testStruct).toBe("Ruedi Zraggen");
			});

			it(title="similar syntax that could conflict: switch", body=function() {
				// switch allow this strange syntax, so Lucee does not allow the function listener operation within this context
				savecontent variable="local.result" {
					switch(mySuccess()) {
					case mySuccess():echo(123);
					}
				}
				expect(result).toBe(123);
			});

			it(title="similar syntax that could conflict: tenary operator", body=function() {
				var b=true;
				var result=b?mySuccess():"not b";
				expect(result).toBe("Susi Sorglos");
			});
			it(title="call the same multiple times", body=function() {
				loop times=2 {
					var t=mySuccess():function(result,error) {
						variables.testFunctionListenerV=result;
						thread.testFunctionListenerV=result;
					};
					threadJoin(t);
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
