component {
	this.name = 'lucee-error-sameFormFieldsAsArray-false';
	this.sameFormFieldsAsArray = false;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}