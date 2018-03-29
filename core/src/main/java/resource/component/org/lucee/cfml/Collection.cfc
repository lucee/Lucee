component extends="Base" accessors="true"{
	// tagname
	variables.tagname = "collection";

	/**
	 * @hint Constructor
	*/
	public Base function init(){
		super.init(argumentCollection=arguments);
		return this;
	}

	/*
	registers the collection with lucee.
	*/
	public void function create(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('create');
		return super.invokeTag();
	}

	/*
	repair
	*/

	public void function repair(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('repair');
		return super.invokeTag();
	}

	/*
	unregisters a collection and deletes its directories.
	*/

	public void function delete(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('delete');
		return super.invokeTag();
	}

	/*
	optimizes the structure and contents of the collection for searching; recovers space. Causes collection to be taken offline, preventing searches and indexing.
	*/

	public void function optimize(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('optimize');
		return super.invokeTag();
	}

	/*
	Returns a query result set, named from the name attribute value, of the attributes of the collections that are registered by lucee.
	*/

	public query function list(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('list');
		return super.invokeTag();
	}

	/*
	creates a map to a collection.
	*/

	public void function map(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('map');
		return super.invokeTag();
	}

	/*
	Retrieves categories from the collection and indicates how many documents are in each one. Returns a structure of structures in which the category representing each substructure is associated with a number of documents
	*/

	public Struct function categorylist(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('categorylist');
		return super.invokeTag();
	}
}
