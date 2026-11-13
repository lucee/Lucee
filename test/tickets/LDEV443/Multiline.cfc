/**
* Component hint
*/
component {

	/**
	* Shutdown the client
	*
	* @timeout.hint The timeout in seconds, we default to 10 seconds
	* 				so that we can do stuff
	*
	* @return A reference to "this" CFC
	*			so that it can work.
	*/
	function shutdown( numeric timeout=10 ){ return this; }

	/**
	* Single line hint
	*
	* @timeout.hint The timeout
	* @return The result
	*/
	function single( numeric timeout=10 ){ return 1; }

	/**
	* First line
	* second line of the function hint
	*
	* @return Something
	*/
	function description(){ return 1; }

}
