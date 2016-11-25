component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		var mode = "ACFcompatibility";
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

				/* FUTURE enable in 5.2 it(title="without list as prefix", body = function( currentSpec ) {
					var mylist = "1,2,3,4,5,6,7,8,9,10";
					try{
						var ReducedVal = mylist.Reduce(
							function(prev, val){
								return prev + val;
							},
							0
						);
					} catch ( any e){
						var ReducedVal = e.message;
					}
					if ( mode == "ACFcompatibility")
						expect(ReducedVal).toBe("The Reduce method was not found.");
					else 
						expect(ReducedVal).toBe(55);
				});*/
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

				/* FUTURE enable in 5.2 it(title="without list as prefix", body = function( currentSpec ) {
					var mylist = "apple|orange|mango";
					var filteredVal = mylist.Filter(
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
				});*/
			});
		});
	}
}
