component javaSettings='{
	"maven": [
		"com.icegreen:greenmail:2.1.7"
	]
}' {
	import "com.icegreen.greenmail.util.ServerSetup";
	import "com.icegreen.greenmail.util.GreenMail";

	private struct function protocolSetups() {
		return {
			smtp: ServerSetup::PROTOCOL_SMTP
			,pop: ServerSetup::PROTOCOL_POP3
			,pops: ServerSetup::PROTOCOL_POP3S
			,imap: ServerSetup::PROTOCOL_IMAP
		};
	}

	// bind port 0 and read back what the OS handed out, so no suite picks a port itself
	public numeric function freePort() {
		var socket = createObject( "java", "java.net.ServerSocket" ).init( 0 );
		var port = socket.getLocalPort();
		socket.close();
		return port;
	}

	// one mock per spec: start( ["smtp"] ) returns the server plus the ports it listens on
	public struct function start( required array protocols ) {
		var setups = [];
		var ports = {};
		for ( var protocol in arguments.protocols ) {
			var port = freePort();
			ports[ protocol ] = port;
			arrayAppend( setups, new ServerSetup( port, nullValue(), protocolSetups()[ protocol ] ) );
		}
		var greenMail = new GreenMail( setups );
		greenMail.start();
		return { server: greenMail, ports: ports };
	}

	public void function stop( required struct greenMail ) {
		arguments.greenMail.server.stop();
	}

	public void function user( required struct greenMail, required string mail, required string password ) {
		arguments.greenMail.server.setUser( arguments.mail, arguments.mail, arguments.password );
	}

	// greenmail counts a latch down when the SMTP manager accepts a message, so this blocks until
	// the mail lands instead of polling the mailbox
	public array function waitForIncoming( required any greenMail, required numeric count, numeric timeout = 10000 ) {
		if ( !arguments.greenMail.waitForIncomingEmail( arguments.timeout, arguments.count ) ) {
			throw "greenmail did not receive #arguments.count# message(s) within #arguments.timeout#ms";
		}
		return arguments.greenMail.getReceivedMessages();
	}

	// for mail greenmail does not own: a POP inbox read back with cfpop, or .eml files on disk
	public any function waitFor( required any probe, required numeric timeout, numeric tick = 25 ) {
		var end = getTickCount() + arguments.timeout;
		while ( true ) {
			var result = arguments.probe();
			if ( !isNull( result ) ) return result;
			if ( getTickCount() > end ) throw "timed out after #arguments.timeout#ms waiting for mail";
			sleep( tick );
		}
	}
}
