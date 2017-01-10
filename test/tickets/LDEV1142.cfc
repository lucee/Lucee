component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1142", body=function(){
			it(title="listToArray with temporary list using QueryObject.columnList", body=function(){
				var tmpQry = querynew("id,name","Integer,Varchar", [ [1,"One"], [2,"Two"], [3,"Three"], [4,"four"], [5,"five"], [6,"six"], [7,"seven"], [8,"eight"] ]);
				var tmpList = tmpQry.columnList
				var tmpArr = tmpList.listToArray();
				expect(tmpArr).toBeArray();
			});

			it(title="listToArray using QueryObject.columnList", body=function(){
				var tmpQry = querynew("id,name","Integer,Varchar", [ [1,"One"], [2,"Two"], [3,"Three"], [4,"four"], [5,"five"], [6,"six"], [7,"seven"], [8,"eight"] ]);
				var tmpArr = tmpQry.columnList.listToArray();
				expect(tmpArr).toBeArray();
			});
		});
	}
}