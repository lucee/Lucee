component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title = "Testcase for createObject() function", body = function() {
			it( title = "Checking the createObject() function", body = function( currentSpec ) {
				var object = createObject('java',"java.lang.StringBuffer")
				expect(isObject(object)).toBeTrue();
				expect(object.length()).toBe(0);
			});

			it( title = "should load a class from bootdelegation with 2 arguments", body = function( currentSpec ) {
                // java.nio.charset is in the bootdelegation list
                var charset = createObject( "java", "java.nio.charset.Charset" );
                
                // Assert the object was created successfully
                expect( isObject(charset) ).toBeTrue();
                
                // Call a method to verify functionality
                var defaultCharset = charset.defaultCharset();
                expect( isObject(defaultCharset) ).toBeTrue();
            });

			it( title = "should load a class outside bootdelegation with 2 arguments", body = function( currentSpec ) {
                // java.util.concurrent.atomic is NOT in the bootdelegation list
                var atomicInteger = createObject( "java", "java.util.concurrent.atomic.AtomicInteger" );
                
                // Assert the object was created successfully
                expect( isObject(atomicInteger) ).toBeTrue();
                
                // Call a method to verify functionality
                var val = atomicInteger.get();
                expect( val ).toBe(0);
            });



			it( title = "should NOT load com.sun.management.HotSpotDiagnosticMXBean with 2 arguments", body = function( currentSpec ) {
                // com.sun.management is NOT in the bootdelegation list
                var bean = createObject( "java", "com.sun.management.HotSpotDiagnosticMXBean" );
                
                // Assert the object was created successfully
                expect( isObject(bean) ).toBeTrue();
            });

			it( title = "should load a class of the loader with 2 arguments", body = function( currentSpec ) {
                var servlet = createObject( "java", "lucee.loader.servlet.CFMLServlet" );
                
                // Assert the object was created successfully
                expect( isObject(servlet) ).toBeTrue();
            });
            
			it( title = "Checking the createObject(..,javasettings:{maven:...}) with jlama", body = function( currentSpec ) {
				
				if ( getJavaVersion() < 21) return;

				var PromptContext=createObject("java","com.github.tjake.jlama.safetensors.prompt.PromptContext",{
					"maven":[
						{
							"groupId" : "com.github.tjake",
							"artifactId" : "jlama-core",
							"version" : "0.7.0"
						},
						{
							"groupId" : "com.github.tjake",
							"artifactId" : "jlama-native",
							"classifier" : "windows-x86_64",
							"version" : "0.7.0"
						}
					]
				});
				expect(PromptContext.of("test").hasTools()).toBeFalse();
			});





			it( title = "Checking the createObject(..,javasettings:{maven:...}) with guava", body = function( currentSpec ) {
				var ImmutableList=createObject("java","com.google.common.collect.ImmutableList",{
					"maven":["com.google.guava:guava:31.0.1-jre"]
				});
				expect(ImmutableList.of("a", "b", "c").size()).toBe(3);
			});

			it( title = "Checking the createObject(..,directory) with guava", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "com.google.guava",
								"artifactId" : "guava",
								"version" : "31.0.1-jre");
				try {
					var ImmutableList=createObject("java","com.google.common.collect.ImmutableList",data.directory);
					expect(ImmutableList.of("a", "b", "c").size()).toBe(3);
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,jars) with guava", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "com.google.guava",
								"artifactId" : "guava",
								"version" : "31.0.1-jre");
				try {
					var ImmutableList=createObject("java","com.google.common.collect.ImmutableList",data.jars);
					expect(ImmutableList.of("a", "b", "c").size()).toBe(3);
				}
				finally {
					cleanup(data);
				}
			});






			it( title = "Checking the createObject(..,javasettings:{maven:...}) with jackson-databind", body = function( currentSpec ) {
				var ObjectMapper=createObject("java","com.fasterxml.jackson.databind.ObjectMapper",{
					"maven":[
						{
							"groupId" : "com.fasterxml.jackson.core",
							"artifactId" : "jackson-databind",
							"version" : "2.12.5"
						}
					]
				});
				expect(ObjectMapper.writeValueAsString({"key":"value"})).toBe('{"key":"value"}');
			});

			it( title = "Checking the createObject(..,directory) with jackson-databind", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "com.fasterxml.jackson.core",
								"artifactId" : "jackson-databind",
								"version" : "2.12.5");
				try {
					var ObjectMapper=createObject("java","com.fasterxml.jackson.databind.ObjectMapper",data.directory);
					expect(ObjectMapper.writeValueAsString({"key":"value"})).toBe('{"key":"value"}');
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,jars) with jackson-databind", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "com.fasterxml.jackson.core",
								"artifactId" : "jackson-databind",
								"version" : "2.12.5");
				try {
					var ObjectMapper=createObject("java","com.fasterxml.jackson.databind.ObjectMapper",data.jars);
					expect(ObjectMapper.writeValueAsString({"key":"value"})).toBe('{"key":"value"}');
				}
				finally {
					cleanup(data);
				}
			});







			it( title = "Checking the createObject(..,javasettings:{maven:...}) with slf4j", body = function( currentSpec ) {
				var Logger=createObject("java","org.slf4j.LoggerFactory",{
					"maven":[
						{
							"groupId" : "org.slf4j",
							"artifactId" : "slf4j-api",
							"version" : "1.7.32"
						}
					]
				});
				expect(Logger.getLogger("TestLogger").isDebugEnabled()).toBeFalse();
			});

			it( title = "Checking the createObject(..,directory) with slf4j", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "org.slf4j",
							"artifactId" : "slf4j-api",
							"version" : "1.7.32");
				try {
					var Logger=createObject("java","org.slf4j.LoggerFactory",data.directory);
					expect(Logger.getLogger("TestLogger").isDebugEnabled()).toBeFalse();
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,jars) with slf4j", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "org.slf4j",
							"artifactId" : "slf4j-api",
							"version" : "1.7.32");
				try {
					var Logger=createObject("java","org.slf4j.LoggerFactory",data.jars);
					expect(Logger.getLogger("TestLogger").isDebugEnabled()).toBeFalse();
				}
				finally {
					cleanup(data);
				}
			});







			it( title = "Checking the createObject(..,javasettings:{maven:...}) with commons-lang3", body = function( currentSpec ) {
				var StringUtils=createObject("java","org.apache.commons.lang3.StringUtils",{
					"maven":[
						{
							"groupId" : "org.apache.commons",
							"artifactId" : "commons-lang3",
							"version" : "3.12.0"
						}
					]
				});
				expect(StringUtils.isEmpty("")).toBeTrue();
			});

			it( title = "Checking the createObject(..,directory) with commons-lang3", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "org.apache.commons",
								"artifactId" : "commons-lang3",
								"version" : "3.12.0");
				try {
					var StringUtils=createObject("java","org.apache.commons.lang3.StringUtils",data.directory);
					expect(StringUtils.isEmpty("")).toBeTrue();
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,jars) with commons-lang3", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "org.apache.commons",
								"artifactId" : "commons-lang3",
								"version" : "3.12.0");
				try {
					var StringUtils=createObject("java","org.apache.commons.lang3.StringUtils",data.jars);
					expect(StringUtils.isEmpty("")).toBeTrue();
				}
				finally {
					cleanup(data);
				}
			});






			it( title = "Checking the createObject(..,javasettings:{maven:...}) with apache-httpclient", body = function( currentSpec ) {
				var HttpClients=createObject("java","org.apache.http.impl.client.HttpClients",{
					"maven":[
						{
							"groupId" : "org.apache.httpcomponents",
							"artifactId" : "httpclient",
							"version" : "4.5.13"
						}
					]
				});
				expect(HttpClients.createDefault().getClass().getSimpleName()).toBe("InternalHttpClient");
			});

			it( title = "Checking the createObject(..,directory) with apache-httpclient", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "org.apache.httpcomponents",
								"artifactId" : "httpclient",
								"version" : "4.5.13");
				try {
					var HttpClients=createObject("java","org.apache.http.impl.client.HttpClients",data.directory);
					expect(HttpClients.createDefault().getClass().getSimpleName()).toBe("InternalHttpClient");
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,jars) with apache-httpclient", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "org.apache.httpcomponents",
								"artifactId" : "httpclient",
								"version" : "4.5.13");
				try {
					var HttpClients=createObject("java","org.apache.http.impl.client.HttpClients",data.jars);
					expect(HttpClients.createDefault().getClass().getSimpleName()).toBe("InternalHttpClient");
				}
				finally {
					cleanup(data);
				}
			});







			it( title = "Checking the createObject(..,javasettings:{maven:...}) with logback-classic", body = function( currentSpec ) {
				var LoggerFactory=createObject("java","ch.qos.logback.classic.Logger",{
					"maven":[
						{
							"groupId" : "ch.qos.logback",
							"artifactId" : "logback-classic",
							"version" : "1.2.11"
						}
					]
				});
				// expect(LoggerFactory.getName()).toBeDefined();
			});

			it( title = "Checking the createObject(..,jars) with logback-classic", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "ch.qos.logback",
								"artifactId" : "logback-classic",
								"version" : "1.2.11");
				try {
					var LoggerFactory=createObject("java","ch.qos.logback.classic.Logger",data.jars);
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,directory) with logback-classic", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "ch.qos.logback",
								"artifactId" : "logback-classic",
								"version" : "1.2.11");
				try {
					var LoggerFactory=createObject("java","ch.qos.logback.classic.Logger",data.directory);
				}
				finally {
					cleanup(data);
				}
			});









			it( title = "Checking the createObject(..,javasettings:{maven:...}) with hamcrest", body = function( currentSpec ) {
				var Matchers=createObject("java","org.hamcrest.Matchers",{
					"maven":[
						{
							"groupId" : "org.hamcrest",
							"artifactId" : "hamcrest",
							"version" : "2.2"
						}
					]
				});
				expect(Matchers.is(1).matches(1)).toBeTrue();
			});

			it( title = "Checking the createObject(..,directory) with hamcrest", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "org.hamcrest",
								"artifactId" : "hamcrest",
								"version" : "2.2");
				try {
					var Matchers=createObject("java","org.hamcrest.Matchers",data.directory);
					expect(Matchers.is(1).matches(1)).toBeTrue();
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,jars) with hamcrest", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "org.hamcrest",
								"artifactId" : "hamcrest",
								"version" : "2.2");
				try {
					var Matchers=createObject("java","org.hamcrest.Matchers",data.jars);
					expect(Matchers.is(1).matches(1)).toBeTrue();
				}
				finally {
					cleanup(data);
				}
			});




			it( title = "Checking the createObject(..,javasettings:{maven:...}) with rxjava", body = function( currentSpec ) {
				var Observable=createObject("java","io.reactivex.rxjava3.core.Observable",{
					"maven":[
						{
							"groupId" : "io.reactivex.rxjava3",
							"artifactId" : "rxjava",
							"version" : "3.0.13"
						}
					]
				});
				expect(Observable.just("Hello").blockingFirst()).toBe("Hello");
			});

			it( title = "Checking the createObject(..,directory) with rxjava", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "io.reactivex.rxjava3",
							"artifactId" : "rxjava",
							"version" : "3.0.13");
				try {
					var Observable=createObject("java","io.reactivex.rxjava3.core.Observable",data.directory);
					expect(Observable.just("Hello").blockingFirst()).toBe("Hello");
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,jars) with rxjava", body = function( currentSpec ) {
				var data=getJarsFor("groupId" : "io.reactivex.rxjava3",
							"artifactId" : "rxjava",
							"version" : "3.0.13");
				try {
					var Observable=createObject("java","io.reactivex.rxjava3.core.Observable",data.jars);
					expect(Observable.just("Hello").blockingFirst()).toBe("Hello");
				}
				finally {
					cleanup(data);
				}
			});


			it( title = "Checking the createObject(..,javasettings:{maven:...}) with maven-core", body = function( currentSpec ) {
				var MissingModuleException=createObject("java","org.apache.maven.MissingModuleException",{
					"maven":[
						{
							"groupId" : "org.apache.maven",
							"artifactId" : "maven-core",
							"version" : "3.8.1"
						}
					]
				});
				var curr=createObject("java","java.io.File").init(getCurrentTemplatePath());
				var msg=MissingModuleException.init("Test",curr,curr).getMessage();
				
				expect(len(msg)>0).toBeTrue();
			});

			it( title = "Checking the createObject(..,directory) with maven-core", body = function( currentSpec ) {
				var data=getJarsFor("org.apache.maven", "maven-core", "3.8.1");
				try {
					var MissingModuleException=createObject("java","org.apache.maven.MissingModuleException",data.directory);
					var curr=createObject("java","java.io.File").init(getCurrentTemplatePath());
					var msg=MissingModuleException.init("Test",curr,curr).getMessage();
					expect(len(msg)>0).toBeTrue();
				}
				finally {
					cleanup(data);
				}
			});

			it( title = "Checking the createObject(..,jars) with maven-core", body = function( currentSpec ) {
				var data=getJarsFor("org.apache.maven", "maven-core", "3.8.1");
				try {
					var MissingModuleException=createObject("java","org.apache.maven.MissingModuleException",data.jars);
					var curr=createObject("java","java.io.File").init(getCurrentTemplatePath());
					var msg=MissingModuleException.init("Test",curr,curr).getMessage();
					expect(len(msg)>0).toBeTrue();
				}
				finally {
					systemOutput(currentspec, true);
					cleanup(data);
				}
			});



		});
	}

	private function cleanup(data){
		try {
			if(directoryExists(arguments.data.directory)) 
				directoryDelete(arguments.data.directory, true);
		} catch( e ){
			if (!isWindows() || (e.message does not contain "delete file" && e.message does not contain "another process")) {
				rethrow;
			} else if (isWindows()) {
				systemOutput("ERROR: LDEV-5145 Windows locked jars", true);
				systemOutput( e.message, true );
				//systemOutput( e.stacktrace, true );
				//rethrow;
				
			}
		}
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}


	private function getJarsFor(groupId,artifactId,version) {
		var jars=mavenLoad({
			"groupId" : groupId,
			"artifactId" : artifactId,
			"version" : version
		});
		
		var trg=expandPath("{temp-directory}/#createUniqueID()#/");
		if(!directoryExists(trg)) directoryCreate(trg);
		var rtn={"jars":[],"directory":trg};
		loop array=jars item="local.jar" {
			var tmp=trg&listLast(jar,"\/");
			arrayAppend(rtn.jars, tmp);
			fileCopy(jar, tmp);
		}
		return rtn;
	}

	private function getJavaVersion() {
		var raw=server.java.version;
		var arr=listToArray(raw,'.');
		if(arr[1]==1) // version 1-9
			return arr[2];
		return listFirst( arr[1], "-" ); // return 25 from java 25-ea
	}
}