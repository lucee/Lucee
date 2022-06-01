component{
	this.name = getTemplatePath();
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan(0,0,30,0);

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}