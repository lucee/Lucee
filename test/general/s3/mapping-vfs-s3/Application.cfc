component {
	this.vfs.s3.lucee.accessKeyId = url.ACCESS_KEY_ID;
	this.vfs.s3.lucee.awsSecretKey = url.SECRET_KEY;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
} 