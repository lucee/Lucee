component {
	private function inner(){
		return {};
	}
	function elvis(){
		return inner().elvis?:'';
	}
	function isItNull(){
		return isNull(inner().isItNull);
	}
}