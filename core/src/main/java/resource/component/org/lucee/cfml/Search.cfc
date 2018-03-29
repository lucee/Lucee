component extends="Base" accessors="true"{
	// tagname
	variables.tagname = "search";

	/**
	 * @hint Constructor
	*/
	public Base function init(){
		super.init(argumentCollection=arguments);
		return this;
	}

	/*
	Executes searches against data indexed
	*/
	public Query function search(){
		this.setAttributes(argumentCollection=arguments);
		return super.invokeTag();
	}
}
