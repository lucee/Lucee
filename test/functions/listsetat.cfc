component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title="Test suite for listSetAt", body=function() {
			it( title='Test case for listSetAt function  ',body=function( currentSpec ) {
				assertEquals("#ListSetAt('aaa,bbb,ccc',1,'xxx')#", "xxx,bbb,ccc");
				assertEquals("#ListSetAt('aaa,bbb,ccc',2,'xxx')#", "aaa,xxx,ccc");
				assertEquals("#ListSetAt('aaa,bbb,ccc',3,'xxx')#", "aaa,bbb,xxx");
				assertEquals("#ListSetAt(' ',1,'xxx')#", "xxx");
				assertEquals("#ListSetAt(',,aaa,bbb,ccc',3,'xxx')#", ",,aaa,bbb,xxx");
				assertEquals("#ListSetAt(',,aaa,,,bbb,ccc',3,'xxx')#", ",,aaa,,,bbb,xxx");

				list="eins,,zwei,drei,vier";
				list=listSetAt(list,2,'new');
				assertEquals("#list#", "eins,,new,drei,vier");
				list=listSetAt(",,,,,,eins,zwei,drei",2,'new');
				assertEquals("#list#", ",,,,,,eins,new,drei");
				list=listSetAt("zero,,,,,,eins,zwei,drei",2,'new');
				assertEquals("#list#", "zero,,,,,,new,zwei,drei");

				sUrl="http://localhost/";
				sMyurl=ListSetAt(sUrl, 2, "pc-cst", '/');
				assertEquals("#ListSetAt(sUrl, 2, "pc-cst", '/')#", "http://pc-cst/");
				assertEquals("#ListSetAt(sUrl, 1, "pc-cst", '/')#", "pc-cst//localhost/");
				assertEquals("#ListSetAt(',,aaa,,,bbb,,,ccc',1,'xxx',',',false)#", ",,xxx,,,bbb,,,ccc");
				assertEquals("#ListSetAt(',,aaa,,,bbb,,,ccc',2,'xxx',',',false)#", ",,aaa,,,xxx,,,ccc");
				assertEquals("#ListSetAt(',,aaa,,,bbb,,,ccc',1,'xxx',',',true)#", "xxx,,aaa,,,bbb,,,ccc");
				assertEquals("#ListSetAt(',,aaa,,,bbb,,,ccc',2,'xxx',',',true)#", ",xxx,aaa,,,bbb,,,ccc");
			});
		});
	}
}