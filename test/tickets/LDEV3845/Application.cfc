component {
    this.name="cfmail-test-ldev-1537";
    this.mailservers = [ {
        host: server.getTestService("smtp").SERVER,
        port: server.getTestService("smtp").PORT_INSECURE,
        username: server.getTestService("smtp").USERNAME,
        password: server.getTestService("smtp").PASSWORD,
        ssl: false,
        tls: false,
        lifeTimespan: createTimeSpan(0,0,1,0),
        idleTimespan: createTimeSpan(0,0,0,10)
    } ];
  }