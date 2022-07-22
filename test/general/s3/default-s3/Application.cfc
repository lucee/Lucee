component {
	this.s3.accessKeyId = url.ACCESS_KEY_ID;
	this.s3.awsSecretKey = url.SECRET_KEY;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
} 