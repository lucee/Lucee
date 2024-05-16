component entityName = 'msSql'  accessors = 'true' persistent = 'true' datasource="testmssql" {
	property name = 'id'	column = 'id'		type	= 'string' fieldtype = 'id' generator="native";
	property name = 'label'	column = 'label'	type	= 'string';

	public any function init() {
		return this;
	}
}