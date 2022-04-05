
component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie,session"	{

	public void function testLoginSession(){
		var res = _InternalRequest(
			template: createURI( "login/login.cfm" )
		);
		dumpCookies( res );
		expect( trim(res.filecontent) ).toBe( "cfloginTestUser" );

		// as cflogin applicationToken is defined, I think the cookie name should be just SECRET, but it comes thru as CFAUTHORIZATION_SECRET, but the docs aren't 100% clear
		var loginCookie = "CFAUTHORIZATION_SECRET"; // defined via applicationToken in cflogin tag

		var sessionCookies = {
			cfid: getCookie( res, "CFID" ),
			cftoken: getCookie( res, "CFTOKEN" ),
		};
		//systemOutput( sessionCookies, true );

		//systemOutput( "----------sessionCookies-------------", true );
		//systemOutput( sessionCookies, true );
	
		expect( getCookie( res, loginCookie ) ).toBe( "--cookie not found--" ); // this.loginstorage = "session";

		//var params =  getCookieParams( res, loginCookie );
		//systemOutput( params, true );

		res = _InternalRequest(
			template: createURI( "login/checkLogin.cfm" ),
			cookies: sessionCookies
		);
		expect( trim( res.filecontent ) ).toBe( "cfloginTestUser" ); // TODO this isn't working

		sleep( 2001 ); // wait for the cflogin idletimeout to expire
		admin
			action="purgeExpiredSessions"
			type="server"
			password="#request.SERVERADMINPASSWORD#";

		res = _InternalRequest(
			template: createURI( "login/checkLogin.cfm" ),
			cookies: sessionCookies
		);
		expect ( res.filecontent ).toBe( "" ); // login should have expired due to session timeout ( not ideltime, that's for cookies)
	}

	public void function testLoginCookieDefault(){
		var res = _InternalRequest(
			template: createURI( "loginDefault/login.cfm" )
		);
		expect( trim(res.filecontent) ).toBe( "cfloginTestUser" );
		dumpCookies( res );
		//systemOutput( res.filecontent, true );

		//systemOutput( res, true );

		var loginCookie = "CFAUTHORIZATION_LOGINDEFAULT"; // applicationToken not defined in cflogin tag, defaults to CFAUTHORIZATION_ & applicationName

		var sessionCookies = {
			cfid: getCookie( res, "CFID" ),
			cftoken: getCookie( res, "CFTOKEN" )
		};
		sessionCookies[ loginCookie ] = getCookie( res, loginCookie );

		//systemOutput( "----------sessionCookies-------------", true );
		//systemOutput( sessionCookies, true );

		expect( getCookie( res, loginCookie ) ).notToBe( "--cookie not found--" ); // this.loginstorage = "cookie";

		var params =  getCookieParams( res, loginCookie );
		//systemOutput( params, true );

		expect( params.domain ).toBe( "lucee.org" );

		res = _InternalRequest(
			template: createURI( "loginDefault/checkLogin.cfm" ),
			cookies: sessionCookies
		);
		expect( trim(res.filecontent) ).toBe( "cfloginTestUser" ); 

		sleep( 2001 ); // wait for the cflogin idletimeout to expire
		admin
			action="purgeExpiredSessions"
			type="server"
			password="#request.SERVERADMINPASSWORD#";

		res = _InternalRequest(
			template: createURI( "loginDefault/checkLogin.cfm" ),
			cookies: sessionCookies
		);
		expect ( trim(res.filecontent) ).toBe( "" ); // login should have expired due to idle timeout, TODO this isn't working
	}
	

	private function dumpCookies(res){
		systemOutput( "", true );
		var c = arguments.res.headers["Set-Cookie"];
		loop array=c item="local.cc"{
			systemOutput( cc, true );
		}
	}

	private function getCookieParams( res, cookieName ){
		var params = {};
		var c = arguments.res.headers["Set-Cookie"];
		loop array=c item="local.cc"{
			if ( listFirst( cc, "=" ) eq arguments.cookieName ){
				loop list=cc item="local.p" delimiters=";" {
					params[ listFirst( p, "=" ) ] = listLast( p, "=" );
				}
				break;
			}
		}
		return params;
	}

	private function getCookie( res, string cookieName, boolean raw=false ){
		var c = arguments.res.headers["Set-Cookie"];
		loop array=c item="local.cc"{
			if ( listFirst( cc, "=" ) eq arguments.cookieName ){
				if ( arguments.raw )
					return cc;
				return listLast( listFirst( cc, ";" ), "=" );
			}
		}
		return "--cookie not found--";
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
