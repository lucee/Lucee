component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for arrayToList()", body=function() {
			it(title="Checking arrayToList() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				arr[1]=111;
				arr[2]=22;
				arr[3]=3.5;

				assertEquals("111,22,3.5",arrayToList(arr));
				assertEquals("111223.5",arrayToList(arr,''));
				assertEquals("111,;22,;3.5",arrayToList(arr,',;'));

				arr[6]="ee";
				assertEquals("111,22,3.5,,,ee",arrayToList(arr));

				arr[7]="e,e";
				assertEquals("111,22,3.5,,,ee,e,e",arrayToList(arr));


				assertEquals("111;22;3.5;;;ee;e,e",arrayToList(arr,";"));

				arr=arrayNew(1);
				arr[1]="a";

				ArrayResize(arr, 10);
				assertEquals("a,,,,,,,,,",arrayToList(arr));

				arr=arrayNew(1);
				arr[1]="a";
				arr[2]="b";

				assertEquals("a{}b",arrayToList(arr,"{}"));

				arr=arrayNew(1);
				arr[4]=111;
				arr[5]=22;
				arr[6]=3.5;

				assertEquals(",,,111,22,3.5",arrayToList(arr));
			});
		});
	}
}