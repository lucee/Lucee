component {
	this.name = "InternalRequest";
	this.sameURLFieldsAsArray=url.sameURLFieldsAsArray?:false;
	this.sameFormFieldsAsArray=url.sameFormFieldsAsArray?:false;
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}