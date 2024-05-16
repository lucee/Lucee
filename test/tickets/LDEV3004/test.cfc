component accessors="true" {
	/**
	* Here is a doc style hint comment.
	* @default "[default]"
	*/
	property name="from" type="string";

	public any function from( required any from ){
		variables.from = arguments.from;
		return this;
	}
}