component initmethod="setup" {
	this.constructed = false;

	function setup() {
		this.setupCalled = true;
		this.constructed = true;
		return this;
	}

	function init() {
		this.initCalled = true;
		this.constructed = true;
		return this;
	}
}
