component accessors=true {

	property name="username" type="string" default="";
	property name="role" type="string" default="guest";

	function setSelf() {
		variables.self = this;
		this.me = this;
	}

	function getPrefs() {
		return variables.prefs;
	}

	function init() {
		variables.prefs = { theme: "light" };
		return this;
	}

}
