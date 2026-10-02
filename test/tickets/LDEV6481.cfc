component extends="org.lucee.cfml.test.LuceeTestCase" labels="component,gateway" {

	function run( testResults, testBox ) {
		describe( "LDEV-6481 ConfigImpl.getBaseComponentPageSource() when Component.cfc is not deployed yet", function() {

			it( title="returns null and logs, instead of throwing a NullPointerException", body=function( currentSpec ) {
				var pc = getPageContext();
				var cfg = pc.getConfig();
				var hidden = hideBaseComponent( cfg );
				expect( arrayLen( hidden ) ).toBeGT( 0, "no Component.cfc found in the component mappings" );
				var result = "";
				var err = "";
				try {
					// force=true skips the cached page source, like the very first lookup on a clean boot
					result = cfg.getBaseComponentPageSource( pc, true );
				}
				catch ( any e ) {
					err = e;
				}
				finally {
					restoreBaseComponent( hidden );
					pagePoolClear();
					cfg.getBaseComponentPageSource( pc, true );
				}
				expect( isSimpleValue( err ) ).toBeTrue( isSimpleValue( err ) ? "" : "getBaseComponentPageSource threw: " & err.message );
				expect( isNull( result ) ).toBeTrue( "expected null when Component.cfc does not exist" );
			});

		});
	}

	// rename every Component.cfc the lookup could find in the component mappings
	private array function hideBaseComponent( required cfg ) {
		var hidden = [];
		for ( var m in arguments.cfg.getComponentMappings() ) {
			var p = m.getPhysical();
			if ( isNull( p ) ) continue;
			for ( var rel in [ "Component.cfc", "org/lucee/cfml/Component.cfc" ] ) {
				var f = p.getRealResource( rel );
				if ( f.exists() ) {
					var bak = f.getAbsolutePath() & ".ldev6481";
					fileMove( f.getAbsolutePath(), bak );
					arrayAppend( hidden, { orig: f.getAbsolutePath(), bak: bak } );
				}
			}
		}
		return hidden;
	}

	private void function restoreBaseComponent( required array hidden ) {
		for ( var h in arguments.hidden ) {
			if ( fileExists( h.bak ) ) fileMove( h.bak, h.orig );
		}
	}
}
