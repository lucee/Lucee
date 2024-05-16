component restpath="/test" rest="true" {

	remote function getBoolean() restpath="getBoolean" {
		return true;
	}

	remote function getComponent() restpath="getComponent" {
		return new comp();
	}

	remote function getNum() httpmethod="GET" {
		return 10;  
	}

	remote function getId() httpMethod="GET" restPath="getId" {
		if( isDefined("id") ) {
			return id;
		}
		cfthrow(type="RestError" message="no id found" errorcode="404");
	}

}