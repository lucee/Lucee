<cfscript>
	// Private functions
	private struct function getCredentials() {
		var s3 = {};
		if(!isNull(server.system.environment.S3_ACCESS_ID) && !isNull(server.system.environment.S3_SECRET_KEY)) {
			// getting the credentials from the environment variables
			s3.ACCESSKEYID=server.system.environment.S3_ACCESS_ID;
			s3.AWSSECRETKEY=server.system.environment.S3_SECRET_KEY;
		}else if(!isNull(server.system.properties.S3_ACCESS_ID) && !isNull(server.system.properties.S3_SECRET_KEY)) {
			// getting the credentials from the system variables
			s3.ACCESSKEYID=server.system.properties.S3_ACCESS_ID;
			s3.AWSSECRETKEY=server.system.properties.S3_SECRET_KEY;
		}
		return s3;
	}

	s3Details = getCredentials();
	mitrahsoftBucketName = "LDEV1176";
	base = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@";
	baseWithBucketName = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@/#mitrahsoftBucketName#";

	path = baseWithBucketName;
	for ( i=1;i<=100;i++){
		path &= "/#i#";
	}
	res = "";
	for( j=1;j<=5;j++){
		local.start = now();
		DirectoryExists(path);
		local.end = now();
		res = listAppend(res, dateDiff("s", local.start, local.end), "|");
	}
	writeOutput(res);
</cfscript>