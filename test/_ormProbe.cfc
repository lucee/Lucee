/**
 * LDEV-6897: probe whether the installed ORM engine can initialize at all.
 *
 * The Maven based hibernate-extension 7.x fails in Hibernate's JakartaJsonIntegration static
 * initializer when no JSON-B provider is on the classpath ("JSON Binding provider
 * org.eclipse.yasson.JsonBindingProvider not found"). That <clinit> failure poisons the
 * JakartaJsonIntegration class for the whole JVM, so every later ORM call fails with
 * NoClassDefFoundError instead of the real message.
 *
 * Probing before the suites run lets the "orm" test service be reported as unavailable (the
 * same way a datasource is verified), so the ORM suites skip with their own noOrm() guard
 * instead of failing 16 times with a stacktrace that hides the cause.
 *
 * ORM needs this.ormEnabled, which the runner application does not set, so the engine is
 * booted in a sub request with its own Application.cfc.
 */
component output="false" {

	/**
	 * @cfg the engine struct from admin action="getORMEngine"
	 * @return a description of the booted engine, throws when the engine cannot initialize
	 */
	public string function verify( struct cfg={} ) localmode=true {
		var r = _InternalRequest( template: "/test/_ormProbe/index.cfm" );
		if ( r.status neq 200 || !findNoCase( "ORM_PROBE_OK", r.fileContent ) )
			throw( message: "ORM engine cannot initialize", detail: trim( r.fileContent ) );
		return "ORM engine: " & ( arguments.cfg.class ?: "installed" ) & " [booted]";
	}

}
