component {
	variables.s3 = server.getTestService("s3");
	this.s3.accessKeyId = variables.s3.ACCESS_KEY_ID;
	this.s3.awsSecretKey = variables.s3.SECRET_KEY;

	param name="url.host" default="";
	param name="url.region" default="";
	 
	if ( len( url.host ) )
		this.s3.host = url.host;

	if ( len( url.region ) )
		this.s3.region = url.region;

} 