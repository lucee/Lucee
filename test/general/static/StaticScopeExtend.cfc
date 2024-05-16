// LDEV-3465
component extends="StaticScopeBase" {

	any function getStaticVariable( required string name ){
		return static[ arguments.name ];
	}

	struct function getStatic(){
		return static;
	}

}