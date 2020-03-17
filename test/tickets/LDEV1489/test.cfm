<cfscript>
	s3Details  = getCredentials();
	mitrahsoftBucketName = "lucee-testsuite-ldev1489";
	base = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@";
	variables.baseWithBucketName = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@/#mitrahsoftBucketName#";
	if(!directoryExists(baseWithBucketName)){
		directoryCreate(baseWithBucketName);
	}
	cffile (action="write", file=baseWithBucketName & "/defaultPermission.txt", output="ACL permission from this.s3.acl");
	acl = StoreGetACL( baseWithBucketName & "/defaultPermission.txt" );
	removeFullControl(acl);
	writeOutput(acl[1].permission & "|" & acl[2].permission);
	
	private function removeFullControl(acl) {
		index=0;
		loop array=acl index="local.i" item="local.el" {
			if(el.permission=="FULL_CONTROL")
				local.index=i;
			
		}
		if(index>0)ArrayDeleteAt( acl, index );
	}

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
</cfscript>