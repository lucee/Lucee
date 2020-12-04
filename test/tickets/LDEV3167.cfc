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
				expect(trim(errorMsg)).toBe("The function [tokey] does not exist in the Object, only the following functions are available: [append,clear,copy,count,delete,duplicate,each,every,filter,find,findKey,findValue,insert,isEmpty,keyArray,keyExists,keyList,keyTranslate,len,map,reduce,some,sort,update].");
			});

			it( title = "Check with invalid array member function", body = function( currentSpec ){
				arr = [1,2,3,4];
				try{
					errorMsg = arr.tokey();
				}
				catch (any e){
					errorMsg = e.message;
				}
				expect(trim(errorMsg)).toBe("The function [tokey] does not exist in the Object, only the following functions are available: [append,avg,clear,contains,containsNoCase,delete,deleteAt,deleteNoCase,duplicate,each,every,filter,find,findAll,findAllNoCase,findNoCase,first,indexExists,insertAt,isDefined,isEmpty,last,len,map,max,median,merge,mid,min,pop,prepend,push,reduce,resize,reverse,set,shift,slice,some,sort,sum,swap,toList,toStruct,unshift].");
			});
			
			it( title = "Check with invalid date member function", body = function( currentSpec ){
				date = now();
				try{
					errorMsg = date.tokey();
				}
				catch (any e){
					errorMsg = e.message;
				}
				expect(trim(errorMsg)).toBe("The function [tokey] does not exist in the Object, only the following functions are available: [add,compare,dateFormat,dateTimeFormat,day,dayOfWeek,dayOfYear,daysInMonth,daysInYear,diff,duplicate,firstDayOfMonth,format,hour,lSDateFormat,lSDateTimeFormat,lsDayOfWeek,lSTimeFormat,millisecond,minute,month,part,quarter,second,setDay,setHour,setMilliSecond,setMinute,setMonth,setSecond,setYear,timeFormat,week,year].");
			});	
		});
	}
}