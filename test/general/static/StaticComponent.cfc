component {
	static.args = [ "table", "name" ];
	static function toSQL() {
		var adapterArgs = structNew("ordered");
		arrayEach(static.args, function( value ) {
			adapterArgs[ arguments.value ] = true;
		});
		return adapterArgs;
	}
}