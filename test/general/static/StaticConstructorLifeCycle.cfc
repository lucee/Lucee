component {
	
	static {
		static.count1=0;
	}
	static {
		static.count2=0;
	}

	static.count1++;

	public function init(){
		static.count2++;
	}

	public static function getCount(){
		return static.count1&"-"&static.count2;
	}
}