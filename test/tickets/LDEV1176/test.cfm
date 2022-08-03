<cfscript>
	// Private functions
	private struct function getCredentials() {
		return server.getTestService("s3");
	}
	param name="url.bucketName";
	s3Details = getCredentials();
	base = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@";
	baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#url.bucketName#";

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