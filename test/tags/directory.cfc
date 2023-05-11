component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll() {
		variables.path = "#getDirectoryFromPath(getCurrenttemplatepath())#dir-test";
		afterAll();
	}
	
	function testDirectory() localmode=true {
		
		// directory create
		directory action="create" directory=path;
		assertTrue(directoryExists(path));
		
		// directory list
		directory action="create" directory="#path#\dir";
		directory action="list" directory=path name="list";
		assertEquals("dir",list.name);

		// directory copy
		directory action="copy" directory="#path#\dir" newDirectory="copyDirectory";
		assertTrue(directoryExists("#path#\copyDirectory"));

		// directory rename
		directory action="rename" directory="#path#\dir" newDirectory="renameDirectory";
		assertFalse(directoryExists("#path#\dir"));
		assertTrue(directoryExists("#path#\renameDirectory"));

		// directory delete
		directory action= "delete" directory="#path#\renameDirectory";
		assertFalse(directoryExists("#path#\renameDirectory"));

		// directory forcedelete
		directory action="forcedelete" directory=path;
		assertFalse(directoryExists(path));

	} 
	
	function afterAll() {
		if(directoryExists(path)) directoryDelete(path,true);
	}
    
}