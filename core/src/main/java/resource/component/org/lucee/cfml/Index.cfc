component extends="Base" accessors="true"{
	// tagname
	variables.tagname = "index";

	/**
	 * @hint Constructor
	*/
	public Base function init(){
		super.init(argumentCollection=arguments);
		return this;
	}

	/*
	updates a collection and adds key to the index	
	*/
	public void function update(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('update');
		return super.invokeTag();
	}

	/*
	deletes all of the documents in a collection.Causes the collection to be taken offline, preventing searches.
	*/

	public void function purge(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('purge');
		return super.invokeTag();
	}

	/*
	removes collection documents as specified by the key attribute.
	*/

	public void function delete(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('delete');
		return super.invokeTag();
	}

	/*
	deletes all of the documents in a collection, and then performs an update
	*/

	public void function refresh(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('refresh');
		return super.invokeTag();
	}

	/*
	Returns a query result set, of indexed data .
	*/

	public query function list(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('list');
		return super.invokeTag();
	}
}
