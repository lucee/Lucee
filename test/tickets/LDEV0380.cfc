component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-380", function() {
			
			it(title = "Checking cfthread action = 'join', from a sister thread", body = function( currentSpec ) {
				request.hasError1 = "";
				thread name="T1" action="run"{
					sleep(100);
				}
				thread name="T2" action="run"{
					sleep(1);
					try{
						thread action="join" name="T1" timeout="5000";
					} catch( any e){
						request.hasError1 = e.message;
					}
				}
				sleep(200);
				expect(request.hasError1).toBe("");
			});

			it(title = "Checking cfthread action = 'join', from a uncle thread", body = function( currentSpec ) {
				request.hasError3 = "";
				thread name="x1" action="run"{
					sleep(100);
				}
				thread name="x2" action="run"{
					thread name="x2.1" action="run"{
						sleep(1);
						try{
							thread action="join" name="x1" timeout="5000";
						} catch( any e){
							request.hasError3 = e.message;
						}
					}
				}
				sleep(200);
				expect(request.hasError3).toBe("");
			});

			it(title = "Checking cfthread action = 'join', from a cousin thread", body = function( currentSpec ) {
				request.hasError4 = "";
				thread name="w1" action="run"{
					thread name="w1.1" action="run"{
						sleep(200);
					}
				}
				thread name="w2" action="run"{
					thread name="w2.1" action="run"{
						sleep(100);
						try{
							thread action="join" name="w1.1" timeout="5000";
						} catch( any e){
							request.hasError4 = e.message;
						}
					}
				}
				sleep(200);
				expect(request.hasError4).toBe("");
			});
			
			it(title = "Checking cfthread action = 'join', from a grand child thread", body = function( currentSpec ) {
				request.hasError2 = "";
				thread name="TT1" action="run"{
					thread name="TT1.1" action="run" {
						sleep(100);
					}
				}
				sleep(50);

				thread action="join" name="TT1.1" timeout="5000";
			});


		});
	}
}