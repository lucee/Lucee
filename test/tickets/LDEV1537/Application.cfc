<cfscript>
    this.name="cfmail-test";
    this.mailservers = [ {
        host: server.getTestService("smtp").SERVER,
      , port: server.getTestService("smtp").INSECURE_PORT,
      , username: server.getTestService("smtp").USERNAME,
      , password: server.getTestService("smtp").PASSWORD
    }];
</cfscript>