<cfscript>
	s3Details  = getCredentials();
	mitrahsoftBucketName = "lucee-testsuite-ldev1489";
	base = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.S3_SECRET_KEY#@";
	variables.baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.S3_SECRET_KEY#@/#mitrahsoftBucketName#";
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
		if(index gt 0) ArrayDeleteAt( acl, index );
	}

	private struct function getCredentials() {
		return server.getTestService("s3");
	}
</cfscript>