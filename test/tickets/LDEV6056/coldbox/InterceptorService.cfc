component extends="BaseService" {

	public function registerInterceptor() {
		return "coldbox.InterceptorService.registerInterceptor";
	}

	// Mimics ColdBox pattern - iterates and calls registerInterceptor via closure
	public function registerInterceptors() {
		var items = [ 1, 2, 3 ];
		var results = [];
		items.each( function( item ) {
			results.append( registerInterceptor() );
		});
		return results;
	}

}
