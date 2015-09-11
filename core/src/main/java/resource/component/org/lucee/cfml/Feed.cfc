component extends="Base" accessors="true"{
	
	// tagname	
	variables.tagname = "feed";
	variables.properties = {};
	
	/* 
	read the feed
	The query,name,xmlvar and properties attributes are optional and overwritten.
	Result is a struct like:
	result = {query = query:Query, name = name:Struct, properties = properties:Struct, xmlvar = xmlVar:XML};
	*/					
	public Struct function read(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('read');
		return super.invokeTag();
	}

	/* 
	create the feed
	The xmlvar attributes is optional and represent the result of the create function.
	*/					

	public Struct function create(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('create');
		return super.invokeTag();
	}
	
	
	public Struct function getFeedProperties(){
		return variables.properties;
	}

	public void function setFeedProperties(Struct properties){
		variables.properties = properties;
	}
						
}
