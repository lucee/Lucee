component initmethod="setup" {
	function setup() {
		this.value = "setup called";
		return this;
	}
	function init() {
		this.value = "init called";
		return this;
	}
}
