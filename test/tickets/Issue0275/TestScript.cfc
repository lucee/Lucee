component {
	static { 
		static1=1;
	}
	static { 
		static2=2; 
	}
	static function getTheStaticScope(){
		return static; 
	} 
	static function getTheStaticScope2(){
		return static; 
	}
}
