component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults , testBox ) {
		describe( title="Test suite for cftimeout Tag",  body=function() {
			describe(title="checking cftimeout tag", body = function( currentSpec ) {
				it(title="no attributes, no timeout", body = function( currentSpec ) {
					savecontent variable="local.result" {
						timeout timespan=1  {
							echo("timeout passing fine");
						}
					}
					systemOutput(result&":timeout passing fine",1,1);
					expect(result).toBe("timeout passing fine");
				});
				
				it(title="no attributes, with timeout", body = function( currentSpec ) {
					var result="";
					try {
						timeout timespan=0.001  {
						   sleep(1000);
						}
					}
					catch(e) {
						result=e.message;
					}
					systemOutput(result&":a timeout occurred within the tag timeout",1,1);
					expect(result).toBe("a timeout occurred within the tag timeout");
				});

				it(title="with attribute ontimeout, with timeout, no escalation", body = function( currentSpec ) {
					savecontent variable="local.result" {
						timeout timespan=0.001 onTimeout=function (timespan) {
							echo("message from within timeout listener");
						}  {
							sleep(1000);
						}
					}
					systemOutput(result&":message from within timeout listener",1,1);
					expect(result).toBe("message from within timeout listener");
				});

				it(title="with attribute ontimeout, with timeout , with escalation", body = function( currentSpec ) {
					var result="";
					try {
						timeout timespan=0.001 onTimeout=function (timespan) {
							throw "escalate the timeout";
						}  {
							sleep(1000);
						}
					}
					catch(e) {
						result=e.message;
					}
					systemOutput(result&":escalate the timeout",1,1);
					expect(result).toBe("escalate the timeout");
				});

				it(title="with attribute onError, with error, no escalation", body = function( currentSpec ) {
					savecontent variable="local.result" {
						timeout timespan=1 onError=function (error) {
							echo("message from within error listener");
						}  {
							throw "Ups!";
						}
					}
					systemOutput(result&":message from within error listener",1,1);
					expect(result).toBe("message from within error listener");
				});

				it(title="with attribute ontimeout, with error , with escalation", body = function( currentSpec ) {
					var result="";
					try {
						timeout timespan=1 onError=function (error) {
							throw "escalate the error";
						}  {
							throw "Ups!";
						}
					}
					catch(e) {
						result=e.message;
					}
					systemOutput(result&":escalate the error",1,1);
					expect(result).toBe("escalate the error");
				});

				/*it(title="test forcestop=true", body = function( currentSpec ) {
					try {
						timeout timespan=0.01 forcestop=false {
						   request.timeouttest="start";
						   sleep(100);
						   request.timeouttest="end";
						}
					}
					catch(e) {}
					sleep(100);
					systemOutput(result&":start",1,1);
					expect(request.timeouttest?:"").toBe("start");
				});

				it(title="test forcestop=false", body = function( currentSpec ) {
					try {
						timeout timespan=0.01 forcestop=false {
						   request.timeouttest="start";
						   sleep(100);
						   request.timeouttest="end";
						}
					}
					catch(e) {}
					sleep(100);
					systemOutput(result&":end",1,1);
					expect(request.timeouttest?:"").toBe("end");
				});*/
				
				
			});
		});
	}
}


