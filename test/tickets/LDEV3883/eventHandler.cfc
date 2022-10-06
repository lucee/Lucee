component implements="org.lucee.cfml.orm.IEventHandler" {

    if ( url.type == "handler" ) isNull(application.test); // Using application scope in event Handler

	public void function preFlush(  entity ){

	}
	public void function postFlush( entity ){

	}

	public void function preLoad( entity ){

	}
	public void function postLoad( entity ){

	}

	public void function preInsert( entity ){

	}
	public void function postInsert( entity ){

	}

	public void function preUpdate( entity, struct olddata){

	}
	public void function postUpdate( entity ){

	}

	public void function preDelete( entity ){

	}	
	public void function postDelete( entity ) {

	}

}