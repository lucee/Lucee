component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql,orm" {
	function run(){
		describe( title="Test suite for LDEV-233", skip=checkMySqlEnvVarsAvailable(), body=function(){
			it(title="Checking ORM with cftransaction", body=function(){
				var uri = createURI("LDEV0233/withTrans.cfm");
				var result = _InternalRequest(
					template:uri
				);
				if(isBoolean(result.fileContent.trim()))
					expect(result.fileContent.trim()).toBeTrue();
				else throw result.fileContent.trim()
			});

			it(title="Checking ORM without cftransaction", body=function(){
				
				




				thread name="showThread" {
					NL="
				";
					ignores=[
						"org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept"
						,"java.lang.Thread.getStackTrace(Thread.java:1559)"
						,"org.apache.tomcat.util.net.NioBlockingSelector$BlockPoller.run"
						,"org.apache.tomcat.util.net.NioEndpoint$Poller.run"
						,"org.apache.catalina.startup.Bootstrap.start"
					];
					loop times=1000 {
						Thread=createObject("java","java.lang.Thread");
						it=Thread.getAllStackTraces().keySet().iterator();
						systemOutput("-------------------------------------------"&NL,1,1);
						// loop threads
						loop collection=it item="t" label="outer" {
							st=t.getStackTrace();
							state=t.getState().toString();
							str="";
							// loop stacktraces
							loop array=st item="ste" {
								str&=ste;
								str&=NL;
							}
				
							if(state=="WAITING" || state=="TIMED_WAITING") continue;
							loop array=ignores item="ignore" {
								if(find(ignore,str))continue "outer";
							}
							if(isEmpty(str)) continue;
				
							
							Systemoutput( ucase( t.name&" ("&state&")" )&NL ,1,1);
							systemOutput(str&NL,1,1);
						}
				
				
				
				
				
						sleep(500);
					}
				}


				
				
				
				
				var uri = createURI("LDEV0233/withoutTrans.cfm");
				var result = _InternalRequest(
					template:uri
				);

				thread action="terminate" name="showThread";
				
				if(isBoolean(result.fileContent.trim()))
					expect(result.fileContent.trim()).toBeTrue();
				else throw result.fileContent.trim()
			});
		});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	private boolean function checkMySqlEnvVarsAvailable() {
		var mySQL= server.getDatasource("mysql");
		return structIsEmpty(mySQL);
	}
}