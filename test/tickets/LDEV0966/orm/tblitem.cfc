component name='tblitem' table='tblitem' persistent=true accessors=true {
	
	property name='id'		ormtype='int'		sqltype='int'		notnull=true	fieldtype='id'	generator='native'	setter=false;
	property name='type'	ormtype='int'		sqltype='int'		notnull=true;
	property name='active'	ormtype='boolean'	sqltype='boolean'	notnull=true	default=true;
	property name='deleted'	ormtype='boolean'	sqltype='boolean'	notnull=true	default=false;
	
}