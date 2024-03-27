<cfscript>
/**
 * This method is attempting to setup the properties metadata of this bean to any properties that the bean inherits from.
 */ 
function setupBase(){
	// calls to the _BaseBean.cfc
	var props = this.collectAllProperties();	

	// get the metadata of this bean
	var md = GetMetaData(this);
	
	// set the metadata of this bean to the collecte all properties
	md["PROPERTIES"] = Duplicate(props);
}
</cfscript>
