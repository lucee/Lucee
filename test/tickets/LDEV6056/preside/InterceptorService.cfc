component extends="coldbox.InterceptorService" {

	public function registerInterceptor() {
		return "preside.InterceptorService -> " & super.registerInterceptor();
	}

	// Override to call parent which uses closure that calls our overridden registerInterceptor
	public function registerInterceptors() {
		return super.registerInterceptors();
	}

}
