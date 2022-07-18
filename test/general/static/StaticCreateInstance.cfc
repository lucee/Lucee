component  {	
	
	public static function newInstance() {
		return new QueryBufferInsert();
	}


	public function test() {
		this.toBeCalled();
		toBeCalled();
		return len(getComponentMetadata(this).functions)==2;
	}

	/**
	* insert a record to the cache (async) or memory
	*/
	public function toBeCalled() {
		
	}					

}