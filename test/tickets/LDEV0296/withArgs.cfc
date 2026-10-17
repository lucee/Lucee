component initmethod="setup" {
	function setup( required string name, numeric age=0, string country="Unknown" ) {
		this.name = arguments.name;
		this.age = arguments.age;
		this.country = arguments.country;
		return this;
	}
}
