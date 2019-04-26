component entityName = 'Foo' table = 'foo' accessors = 'true' persistent = 'true' cacheuse = 'transactional'{
	property name = 'id'	column = 'foo_id'		type	= 'string'		length = '32' fieldtype = 'id' generator = 'assigned';
	property name = 'label'	column = 'foo_label'	type	= 'string'		length = '32';

	public Foo function init() {
		return this;
	}

}