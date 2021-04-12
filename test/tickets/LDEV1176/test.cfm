<cfscript>
	// Private functions
	private struct function getCredentials() {
		return server.getTestService("s3");
	}

	s3Details = getCredentials();
	mitrahsoftBucketName = "ldev1176";
	base = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.S3_SECRET_KEY#@";
	baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.S3_SECRET_KEY#@/#mitrahsoftBucketName#";

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