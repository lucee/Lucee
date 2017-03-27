component output="false" displayname="Static"  {

	static {
		static.data = {
			foo = 'bar',
			bar = 'foo'
		};
	}

	public static struct function getData() {
		return static.data;
	}

	public function init() {
		return this;
	}

	public function foo(){
		return 'bar';
	}
}