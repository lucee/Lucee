component extends="org.lucee.cfml.test.LuceeTestCase" labels="ftp"	{
		
    private void function _test(required boolean secure, required string host, required number port=21,
        required string user, required string pass, required string base, required boolean stopOnError){

        systemOutput(arguments, true);

        expect(function(){
            ftp action = "open"
                connection="ftpConn"
                passive = "true"
                secure = arguments.secure
                username = arguments.user
                password = arguments.pass
                server = arguments.host
                stopOnError = arguments.stopOnError
                port = arguments.port;
        }).notToThrow();
    }

    public function testSFTPstopOnError() {
        var sftp=getSFTPCredentials();
        if(!structCount(sftp)) return;
        //return; //disable failing test
        _test(
            secure: true,
            host: sftp.server,
            user: sftp.username,
            pass: sftp.password,
            port: sftp.port,
            base: sftp.base_path,
            stopOnError: true
        );
        _test(
            secure: true,
            host: sftp.server,
            user: sftp.username,
            pass: sftp.password,
            port: sftp.port,
            base: sftp.base_path,
            stopOnError: false
        );
    }

    public function testSFTPstopOnError() {
        var ftp=getFTPCredentials();
        if(!structCount(ftp)) return;
        //return; //disable failing test
        _test(
            secure: false,
            host: ftp.server,
            user: ftp.username,
            pass: ftp.password,
            port: ftp.port,
            base: ftp.base_path,
            stopOnError: true
        );
        _test(
            secure: false,
            host: ftp.server,
            user: ftp.username,
            pass: ftp.password,
            port: ftp.port,
            base: ftp.base_path,
            stopOnError: false
        );
    }

    private struct function getFTPCredentials() {
        return server.getTestService("ftp");
    }

    private struct function getSFTPCredentials() {
        return server.getTestService("sftp");
    }
} 
    