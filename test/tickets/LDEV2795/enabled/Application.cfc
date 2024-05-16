component {
	this.name = 'lucee-error-sameFormFieldsAsArray';
	this.sameFormFieldsAsArray = true;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}