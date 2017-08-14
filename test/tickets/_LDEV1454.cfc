component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-14544", function() {
			it(title = "Checking HTTP call, with Content-Type header value as multipart/form-data", body = function( currentSpec ) {
				var formData = {
					"foo" : "bar",
					"foo2" : "bar2"
				}
				var echo = "http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1454/";

				var h1 = new http(
					url=echo,
					method="POST"
				);

				for( key in formData ){
					h1.addParam( type="formfield",  name=key, value=formData[ key ] );
				}
				h1.addParam( type="header", name="Content-Type", value="multipart/form-data" );

				var result = h1.send().getPrefix().filecontent ;
				expect(result).toBe('{"foo2":"bar2","foo":"bar","fieldnames":"foo2,foo"}');
			});

			it(title = "Checking HTTP call, with Content-Type header value as default value", body = function( currentSpec ) {
				var formData = {
					"foo" : "bar",
					"foo2" : "bar2"
				}
				var echo = "http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1454/";

				var h1 = new http(
					url=echo,
					method="POST"
				);

				for( key in formData ){
					h1.addParam( type="formfield",  name=key, value=formData[ key ] );
				}

				var result = h1.send().getPrefix().filecontent ;
				expect(result).toBe('{"foo2":"bar2","foo":"bar","fieldnames":"foo2,foo"}');
			});

		});
	}
}