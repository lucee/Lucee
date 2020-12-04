component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run ( testResults , testbox ){
		describe( "Testcase for LDEV-3167", function(){
			it( title = "Check with invaild struct member function", body = function( currentSpec ){
				str = { one : "one",two : "two" };
				try{
					errorMsg = str.tokey();
				}
				catch (any e){
					errorMsg = e.message;
				}

				// append,clear,copy,count,delete,duplicate,each,every,filter,find,findKey,findValue,insert,isEmpty,keyArray,keyExists,keyList,keyTranslate,len,map,reduce,some,sort,update

				expect(findNocase("toJson",errorMsg)>0).toBe(true);
				expect(findNocase("keyList",errorMsg)>0).toBe(true);
			});

			it( title = "Check with invalid array member function", body = function( currentSpec ){
				arr = [1,2,3,4];
				try{
					errorMsg = arr.tokey();
				}
				catch (any e){
					errorMsg = e.message;
				}

				expect(findNocase("toJson",errorMsg)>0).toBe(true);
				expect(findNocase("indexExists",errorMsg)>0).toBe(true);
			});
			
			it( title = "Check with invalid date member function", body = function( currentSpec ){
				date = now();
				try{
					errorMsg = date.tokey();
				}
				catch (any e){
					errorMsg = e.message;
				}
				expect(findNocase("toJson",errorMsg)>0).toBe(true);
				expect(findNocase("dayOfWeek",errorMsg)>0).toBe(true);
			});	
		});
	}
}