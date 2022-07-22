component {
	this.name =	"LDEV-1489";
	this.s3.acl = "public-read-write";
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}
}
