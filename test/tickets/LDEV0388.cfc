component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-388", function() {
			describe(title="checking listreduce() working in member function", body = function( currentSpec ) {
				
				it(title="Having list as prefix", body = function( currentSpec ) {
					var mylist = "1,2,3,4,5,6,7,8,9,10";
					try{
						var ReducedVal = mylist.listReduce(
							function(prev, val){
								return prev + val;
							},
							0
						);
					} catch ( any e){
						var ReducedVal = e.message;
					}
					expect(ReducedVal).toBe(55);
				});

				it(title="without list as prefix", body = function( currentSpec ) {
					var mylist = "1,2,3,4,5,6,7,8,9,10";
					var expMsg="";
					try{
						var ReducedVal = mylist.Reduce(
							function(prev, val){
								return prev + val;
							},
							0
						);
					} catch ( any e){
						expMsg = e.message;
					}
					expect(expMsg GT 0).toBe(true);

				});
			});

			describe(title="checking listFilter() working in member function", body = function( currentSpec ) {
				it(title="Having list as prefix", body = function( currentSpec ) {
					var mylist = "apple|orange|mango";
					var filteredVal = mylist.listFilter(
						function(elem, idx){
							if(elem != "orange" ){
								return true;
							}
							return  false;
						}, "|"
					);
					expect(filteredVal).toBe("apple|mango");
				});

				it(title="without list as prefix", body = function( currentSpec ) {
					var mylist = "apple|orange|mango";
					var expMsg="";
					try {
						var filteredVal = mylist.Filter(
							function(elem, idx){
								if(elem != "orange" ){
									return true;
								}
								return  false;
							}, "|"
						);
					} catch ( any e){
						expMsg = e.message;
					}
					expect(expMsg GT 0).toBe(true);
				 
				});



			});
		});
	}
}
