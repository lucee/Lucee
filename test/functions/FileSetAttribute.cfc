component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.base = GetDirectoryFromPath(getcurrentTemplatepath());
        variables.path = base&"fileSetAttribute";
        if(!directoryExists(base&"fileSetAttribute")){
            directoryCreate(base&'fileSetAttribute');
        }
    }

    function isNotSupported() {
        var isWindows =find("Windows",server.os.name);
        if(isWindows > 0 ) return false;
        else return  true;
    }

    function afterAll(){
        if(directoryExists(base&"fileSetAttribute")){
            directoryDelete(base&"fileSetAttribute",true);
        }
    }
    function run( testResults , testBox ) {
        describe( title="Testcase for LDEV-1880", skip=isNotSupported(),  body = function() {
            beforeEach( function( currentSpec ) {
                if(!fileExists(path&"/example_LDEV1880.txt")){
                    variables.myfile = FileOpen(path, "write");
                    FileWrite(path&"/example_LDEV1880.txt","This is a sample file content");
                }
            });
            afterEach( function( currentSpec ) {
                if(fileExists(path&"/example_LDEV1880.txt")){
                    filedelete(path&"/example_LDEV1880.txt");
                }
            });

            it(title = "checking the file with Archive Attribute", body = function( currentSpec ) {
                fileSetAttribute(path&"/example_LDEV1880.txt",'Archive');
                expect(getfileinfo(path&"/example_LDEV1880.txt").isArchive).toBe('true');
            });
            it(title = "checking the file with System Attribute", body = function( currentSpec ) {
                fileSetAttribute(path&"/example_LDEV1880.txt",'System');
                expect(getfileinfo(path&"/example_LDEV1880.txt").isSystem).toBe('true');
            });
            it(title = "checking the file with readOnly Attribute", body = function( currentSpec ) {
                fileSetAttribute(path&"/example_LDEV1880.txt",'readOnly');
                expect(getfileinfo(path&"/example_LDEV1880.txt").canRead).toBe('true');
                expect(getfileinfo(path&"/example_LDEV1880.txt").canWrite).toBe('false');
            });
            it(title = "checking the file with Hidden Attribute", body = function( currentSpec ) {
                fileSetAttribute(path&"/example_LDEV1880.txt",'Hidden');
                expect(getfileinfo(path&"/example_LDEV1880.txt").isHidden).toBe('true');
            });
            it(title = "checking the file with Normal Attribute", body = function( currentSpec ) {
                fileSetAttribute(path&"/example_LDEV1880.txt",'Normal');
                expect(getfileinfo(path&"/example_LDEV1880.txt").canRead).toBe('true');
                expect(getfileinfo(path&"/example_LDEV1880.txt").canWrite).toBe('true');
                expect(getfileinfo(path&"/example_LDEV1880.txt").isHidden).toBe('false');
                expect(getfileinfo(path&"/example_LDEV1880.txt").isSystem).toBe('false');
                expect(getfileinfo(path&"/example_LDEV1880.txt").isArchive).toBe('false');
            });
        });
        describe( "Testcase for LDEV-2410", function() {
            it(title = "checking the file with READONLY Attribute", body = function( currentSpec ) {
                FileWrite(path&"\example_LDEV2410.txt","I am in readonly file");
                fileSetAttribute(path&"\example_LDEV2410.txt",'readonly');
                expect(getfileinfo(path&"\example_LDEV2410.txt").canRead).toBe(true);
                expect(getfileinfo(path&"\example_LDEV2410.txt").canWrite).toBe(false);
            });
            it(title = "Checking changing file attribute to NORMAL from READONLY", body = function( currentSpec ) {
                try{
                    fileSetAttribute(path&"\example_LDEV2410.txt",'normal');
                    FileWrite(path&"\example_LDEV2410.txt","I am in normal file");
                    res = getfileinfo(path&"\example_LDEV2410.txt").canWrite;
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe(true);
            });
        });
        describe( title="Testcase for LDEV-2349", body=function() {
            it( title="Checking FileCopy- Destination file access mode with file attribute readonly",body=function( currentSpec ) {
                filewrite(path&"\newfile.txt","This is new file");
                filesetattribute(path&"\newfile.txt","readonly");
                filecopy(path&"\newfile.txt",path&"\desFile.txt");
                assertEquals("FALSE",getfileinfo(path&"\newfile.txt").canwrite);
                assertEquals("FALSE",getfileinfo(path&"\desFile.txt").canwrite);
            });
            it( title="Checking FileCopy- Destination file access mode with file attribute hidden",skip=isNotSupported(),body=function( currentSpec ) {
                filewrite(path&"\newfile1.txt","This is new file");
                filesetattribute(path&"\newfile1.txt","hidden");
                filecopy(path&"\newfile1.txt",path&"\desFile1.txt");
                assertEquals("TRUE",getfileinfo(path&"\newfile1.txt").ishidden);
                assertEquals("TRUE",getfileinfo(path&"\desFile1.txt").ishidden);
            });
            it( title="Checking FileCopy- Destination file access mode with file attribute normal",body=function( currentSpec ) {
                filewrite(path&"\newfile2.txt","This is new file");
                filesetattribute(path&"\newfile2.txt","normal");
                filecopy(path&"\newfile2.txt",path&"\desFile2.txt");
                assertEquals("FALSE",getfileinfo(path&"\newfile2.txt").ishidden);
                assertEquals("FALSE",getfileinfo(path&"\desFile2.txt").ishidden);
                assertEquals("TRUE",getfileinfo(path&"\newfile2.txt").canwrite);
                assertEquals("TRUE",getfileinfo(path&"\desFile2.txt").canwrite);
            });
        });
    }
}
