component entityName = 'mySql' table = 'mysql' accessors = 'true' persistent = 'true' datasource="testMYSQL" {
	property name = 'id'	column = 'id'		type	= 'string' fieldtype = 'id' generator = 'assigned';
	property name = 'label'	column = 'label'	type	= 'string';

	public any function init() {
		return this;
	}
}