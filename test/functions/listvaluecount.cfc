component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title="Test suite for listValueCount", body=function() {
			it( title='Test case for listValueCount function  ',body=function( currentSpec ) {
				assertEquals("#ListValueCount('aaa,bbb,bbb,ccc,bbb','aaa')#", "1");
				assertEquals("#ListValueCount('aaa,bbb,bbb,ccc,bbb','bbb')#", "3");
				assertEquals("#ListValueCount('aaa,bbb,bbb,ccc,bbb','xxx')#", "0");
				assertEquals("#ListValueCount('AAA,aaa,bbb,bbb,ccc,bbb','aaa')#", "1");
				assertEquals("#ListValueCount(',,,,AAA,aaa,bbb,bbb,ccc,bbb','aaa')#", "1");
				assertEquals("#ListValueCount('','aaa')#", "0");
				assertEquals("#ListValueCount('a,,b','',',')#", "0");
				assertEquals("#ListValueCount('a,,b','',',',false)#", "0");
				assertEquals("#ListValueCount('a,,b','',',',true)#", "1");
				assertEquals("#ListValueCount(',a,,b,','',',',true)#", "3");
			});

			it( title='Test case for listValueCount function  ',body=function( currentSpec ) {
				assertEquals("#'aaa,bbb,bbb,ccc,bbb'.ListValueCount('aaa')#", "1");
				assertEquals("#'aaa,bbb,bbb,ccc,bbb'.ListValueCount('bbb')#", "3");
				assertEquals("#'aaa,bbb,bbb,ccc,bbb'.ListValueCount('xxx')#", "0");
				assertEquals("#'AAA,aaa,bbb,bbb,ccc,bbb'.ListValueCount('aaa')#", "1");
				assertEquals("#',,,,AAA,aaa,bbb,bbb,ccc,bbb'.ListValueCount('aaa')#", "1");
				assertEquals("#''.ListValueCount('aaa')#", "0");
				assertEquals("#'a,,b'.ListValueCount('',',')#", "0");
				assertEquals("#'a,,b'.ListValueCount('',',',false)#", "0");
				assertEquals("#'a,,b'.ListValueCount('',',',true)#", "1");
				assertEquals("#',a,,b,'.ListValueCount('',',',true)#", "3");
			});
		});
	}
}

listValueCount
Listvaluecountnocase
listsetat
listsort
listValueCount