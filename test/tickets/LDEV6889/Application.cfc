component {
	this.name = "LDEV6889_" & hash( getCurrentTemplatePath() );
	this.formUrlAsStruct = true;
	// Arrays here must come from the [] marker, not from duplicate-key collapsing.
	this.sameFormFieldsAsArray = false;
	this.sameURLFieldsAsArray = false;
}
