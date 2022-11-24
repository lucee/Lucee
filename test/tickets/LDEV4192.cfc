component extends="org.lucee.cfml.test.LuceeTestCase" labels="ftp"	{
		
    private void function _test(required boolean secure, required string host, required number port=21,
        required string user, required string pass, required string base, required boolean stopOnError, required boolean passive){

        var args = arguments;

        expect(function(){
            ftp action = "open"
                connection="ftpConn"
                passive = args.passive
                timeout = 5
                secure = args.secure
                username = args.user
                password = args.pass
                server = args.host
                stopOnError = args.stopOnError
                port = args.port;
        }).notToThrow();

        expect(function(){
            var ftpService = new ftp(
                Username : args.user,
                Password : args.pass,
                Server : args.host,
                Connection : 'myConn',
                Port : args.port,
                timeout : 5,
                Secure : args.secure,
                passive = args.passive,
                StopOnError : args.stopOnError
            );
            ftpService.open();
        }).notToThrow();
        systemOutput("passed #args.toJson()#", true );
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
            stopOnError: true,
            passive: true
        );
        _test(
            secure: true,
            host: sftp.server,
            user: sftp.username,
            pass: sftp.password,
            port: sftp.port,
            base: sftp.base_path,
            stopOnError: false,
            passive: true
        );
        _test(
            secure: true,
            host: sftp.server,
            user: sftp.username,
            pass: sftp.password,
            port: sftp.port,
            base: sftp.base_path,
            stopOnError: true,
            passive: false
        );
        _test(
            secure: true,
            host: sftp.server,
            user: sftp.username,
            pass: sftp.password,
            port: sftp.port,
            base: sftp.base_path,
            stopOnError: false,
            passive: false
        );
    }

    public function testFTPstopOnError() {
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
            stopOnError: true,
            passive: true
        );
        _test(
            secure: false,
            host: ftp.server,
            user: ftp.username,
            pass: ftp.password,
            port: ftp.port,
            base: ftp.base_path,
            stopOnError: false,
            passive: true
        );
        _test(
            secure: false,
            host: ftp.server,
            user: ftp.username,
            pass: ftp.password,
            port: ftp.port,
            base: ftp.base_path,
            stopOnError: true,
            passive: false
        );
        _test(
            secure: false,
            host: ftp.server,
            user: ftp.username,
            pass: ftp.password,
            port: ftp.port,
            base: ftp.base_path,
            stopOnError: false,
            passive: false
        );
    }

    private struct function getFTPCredentials() {
        return server.getTestService("ftp");
    }

    private struct function getSFTPCredentials() {
        return server.getTestService("sftp");
    }
} 
    