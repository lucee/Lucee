component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		

		describe( "test suite for LDEV2748", function() {
			it(title = "checking property syntax", body = function( currentSpec ) {
				var test=new LDEV2748.Test();

				var meta=getMetaData(test);
				var arrProps=meta.properties;
				var props={};
				loop array=arrProps item="p" {
					props[p.name]=p;
				}

				expect(props.a.type).toBe('any');
				expect(props.b.type).toBe('string');
				expect(props.c.type).toBe('a.b');
				expect(props.d.type).toBe('string');
				expect(props.d.e).toBe('');
				expect(props.d.e).toBe('');
				expect(props.f.g).toBe('h');
				expect(props.i.type).toBe('any');
				expect(props.j.type).toBe('number');
				expect(props.k.type).toBe('number');
				expect(props.k.default).toBe('val');
			});

		});
	}
} 