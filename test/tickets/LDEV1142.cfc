component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1142", body=function(){
			it(title="QueryObject.ColumnList variable for member Function", body=function(){
				var query = querynew("id,name","Integer,Varchar", [ [1,"One"], [2,"Two"], [3,"Three"], [4,"four"], [5,"five"], [6,"six"], [7,"seven"], [8,"eight"] ]);
				var array = query.columnList.listToArray();
				expect(arrray).toBeArray();
			});
		});
	}
}