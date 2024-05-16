component {
    this.name = "App6";
    this.customSerializer="custom.Serialize";

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}