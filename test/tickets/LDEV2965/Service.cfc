component {

	remote string function getFileName() returnformat="plain" {
		return "report.txt";
	}

	remote string function getLine() returnformat="plain" {
		return "line" & chr( 10 );
	}

	remote string function getBuffered() returnformat="plain" {
		writeOutput( "BUFFERED-" );
		return "RETURN";
	}

	remote string function ping() {
		return "pong";
	}

	public void function onRequestStart() {
	}

	public string function sharedFn() {
		return "shared";
	}

	package string function packageFn() {
		return "package";
	}

	private string function localFn() {
		return "local";
	}

}
