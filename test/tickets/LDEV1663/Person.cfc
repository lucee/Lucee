component name = "Person" output = false accessors = true implements = "test"{
	property name = "name" type = "string";

	public Person function init(required string name){
		this.name = name;
		return this;
	}
}