component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll(){
		variables.base = GetDirectoryFromPath(getcurrentTemplatepath()) & "LDEV2152\";
		if(!directoryExists(base)){
			directorycreate(base);
		}
		var dirList = "b,n";
		dirlist.each(function(index){
			directorycreate(base&index);
			if(index is "b"){
				directorycreate(base&'b\d');
			}
		})
		var fileList = "a.txt,c.txt,j.txt";
		var fileList.each(function(index){
			FileWrite(base&index,"");
			FileWrite(base&'b\e.txt',"");
			filewrite(base&'b\d\g.txt',"");
			filewrite(base&'b\d\p.txt',"");
			filewrite(base&'n\h.txt',"");
			filewrite(base&'n\o.txt',"");
		})
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2152", function() {
			it(title = "directorylist() with attribute listinfo = 'name'", body = function( currentSpec ) {
				var dirList = directorylist(base,true,'name','*.txt','directory ASC');
				expect(dirList[1]).toBe('a.txt');
				expect(dirList[2]).toBe('c.txt');
				expect(dirList[3]).toBe('j.txt');
				expect(dirList[4]).toBe('e.txt');
				expect(dirList[5]).toBe('g.txt');
				expect(dirList[6]).toBe('p.txt');
				expect(dirList[7]).toBe('h.txt');
				expect(dirList[8]).toBe('o.txt');
			});

			it(title = "directorylist() with attribute listinfo = 'name',sort = 'desc'", body = function( currentSpec ) {
				var dirList = directorylist(base,true,'name','*.txt','directory DESC');
				expect(dirList[1]).toBe('h.txt');
				expect(dirList[2]).toBe('o.txt');
				expect(dirList[3]).toBe('g.txt');
				expect(dirList[4]).toBe('p.txt');
				expect(dirList[5]).toBe('e.txt');
				expect(dirList[6]).toBe('a.txt');
				expect(dirList[7]).toBe('c.txt');
				expect(dirList[8]).toBe('j.txt');
			});
		});
	}

	function afterAll(){
		if(directoryExists(base)){
			directoryDelete(base,true);
		}
	}
}