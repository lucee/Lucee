component {
    this.name = "LDEV-2862";
    this.ormEnabled = "true";
    this.ormSettings = {
        dbCreate = "dropcreate"
    }

    this.datasource = server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db");

    public function onRequestStart() {
        query result="test"{
            echo("INSERT INTO test(A) VALUES( 'testA' )");
        }
        query result="test2"{
            echo("INSERT INTO test2(B) VALUES( 'testB' )");
        }
        query {
            echo("INSERT INTO okok(testid, id) VALUES( #test.GENERATEDKEY#, #test2.GENERATEDKEY# )");
        }
    }

    function onRequestEnd() {
        var javaIoFile=createObject("java","java.io.File");
        loop array=DirectoryList(
            path=getDirectoryFromPath(getCurrentTemplatePath()), 
            recurse=true, filter="*.db") item="local.path"  {
            fileDeleteOnExit(javaIoFile,path);
        }
    }

    private function fileDeleteOnExit(required javaIoFile, required string path) {
        var file=javaIoFile.init(arguments.path);
        if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
        if(file.isFile()) file.deleteOnExit();
    }
}