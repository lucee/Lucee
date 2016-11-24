component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		mode = "ACFcompatibility";
		describe( "Test suite for LDEV-388", function() {
			describe(title="checking listreduce() working in member function", body = function( currentSpec ) {
				it(title="Having list as prefix", body = function( currentSpec ) {
					mylist = "1,2,3,4,5,6,7,8,9,10";
					try{
						ReducedVal = mylist.listReduce(
							function(prev, val){
								return prev + val;
							},
							0
						);
					} catch ( any e){
						ReducedVal = e.message;
					}
					expect(ReducedVal).toBe(55);
				});

				it(title="without list as prefix", body = function( currentSpec ) {
					mylist = "1,2,3,4,5,6,7,8,9,10";
					try{
						ReducedVal = mylist.Reduce(
							function(prev, val){
								return prev + val;
							},
							0
						);
					} catch ( any e){
						ReducedVal = e.message;
					}
					if ( mode == "ACFcompatibility")
						expect(ReducedVal).toBe("The Reduce method was not found.");
					else 
						expect(ReducedVal).toBe(55);
				});
			});

			describe(title="checking listFilter() working in member function", body = function( currentSpec ) {
				it(title="Having list as prefix", body = function( currentSpec ) {
					mylist = "apple|orange|mango";
					filteredVal = mylist.listFilter(
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
					mylist = "apple|orange|mango";
					filteredVal = mylist.Filter(
						function(elem, idx){
							if(elem != "orange" ){
								return true;
							}
							return  false;
						}, "|"
					);
					if ( mode == "ACFcompatibility")
						expect(filteredVal).toBe("The Filter method was not found.");
				 	else 
						expect(filteredVal).toBe("apple|mango");
				});
			});
		});
	}
}
