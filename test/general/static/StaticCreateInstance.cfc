component  {	
	
	public static function newInstance() {
		return new QueryBufferInsert();
	}
	public function test() {
		this.toBeCalled();
		toBeCalled();
		return len(getComponentMetadata(this).functions)==2;
	}
	public function toBeCalled() {
		
	}					

}