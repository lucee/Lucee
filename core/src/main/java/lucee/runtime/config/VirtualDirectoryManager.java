package lucee.runtime.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;

/**
 * Manages virtual directory mappings from x-vdirs header (mod_cfml).
 *
 * Virtual directories are parsed from the x-vdirs header format:
 * /virtual1,/physical/path1;/virtual2,/physical/path2;
 *
 * Security: Requires x-vdirs-sharedkey header to match lucee.vdirs.sharedkey configuration.
 * Feature is disabled if lucee.vdirs.sharedkey is not configured.
 */
public class VirtualDirectoryManager {

	private static final String CONFIGURED_SHARED_KEY = SystemUtil.getSystemPropOrEnvVar( "lucee.vdirs.sharedkey",
			"" );

	private final ConfigWeb config;

	// Cache: x-vdirs header value hash -> array of Mapping objects
	private final Map<Integer, Mapping[]> cache = new ConcurrentHashMap<>( 4 );

	private volatile boolean invalidKeyWarned = false;

	public VirtualDirectoryManager( ConfigWeb config ) {
		this.config = config;
	}

	/**
	 * Gets virtual directory mappings for the current request.
	 * Returns null if feature is disabled or validation fails.
	 *
	 * @param pc PageContext for current request
	 * @return Array of Mapping objects, or null
	 */
	public Mapping[] getVirtualDirectoryMappings( PageContext pc ) {
		// Feature disabled if no shared key configured
		if ( StringUtil.isEmpty( CONFIGURED_SHARED_KEY ) ) {
			return null;
		}

		// Read x-vdirs header directly from servlet request (avoids lazy-loading CGI scope)
		String xVdirs = pc.getHttpServletRequest().getHeader( "x-vdirs" );

		// No virtual directories in this request
		if ( StringUtil.isEmpty( xVdirs ) ) {
			return null;
		}

		// Validate shared key
		String xVdirsSharedKey = pc.getHttpServletRequest().getHeader( "x-vdirs-sharedkey" );
		if ( StringUtil.isEmpty( xVdirsSharedKey ) || !CONFIGURED_SHARED_KEY.equals( xVdirsSharedKey ) ) {
			if ( !invalidKeyWarned ) {
				invalidKeyWarned = true;
				Log log = config.getLog( "application" );
				if ( log != null ) {
					log.error( "VirtualDirectoryManager",
							"x-vdirs header received but x-vdirs-sharedkey is missing or doesn't match configured shared key. Ignoring virtual directories." );
				}
			}
			return null;
		}

		// Check cache
		int cacheKey = xVdirs.hashCode();
		Mapping[] cached = cache.get( cacheKey );
		if ( cached != null ) {
			return cached;
		}

		// Parse and cache
		Mapping[] mappings = parseVirtualDirectories( xVdirs );
		if ( mappings != null && mappings.length > 0 ) {
			cache.put( cacheKey, mappings );
		}

		return mappings;
	}

	/**
	 * Parses x-vdirs header format: /virtual1,/physical1;/virtual2,/physical2;
	 *
	 * @param xVdirs Header value
	 * @return Array of Mapping objects
	 */
	private Mapping[] parseVirtualDirectories( String xVdirs ) {
		if ( StringUtil.isEmpty( xVdirs ) ) {
			return null;
		}

		// Split by semicolon
		String[] entries = xVdirs.split( ";" );
		if ( entries.length == 0 ) {
			return null;
		}

		// Parse each entry
		List<Mapping> mappings = new ArrayList<>();
		for ( String entry : entries ) {
			entry = entry.trim();
			if ( entry.isEmpty() ) {
				continue;
			}

			// Split by comma
			String[] parts = entry.split( ",", 2 );
			if ( parts.length != 2 ) {
				continue;
			}

			String virtual = parts[0].trim();
			String physical = parts[1].trim();

			// Skip invalid entries
			if ( virtual.length() <= 1 || physical.length() <= 1 ) {
				continue;
			}

			// Convert Windows backslashes to forward slashes
			physical = physical.replace( '\\', '/' );

			// Create mapping
			try {
				Resource physicalResource = ResourceUtil.toResourceExisting( config, physical );
				if ( physicalResource != null && physicalResource.exists() ) {
					Mapping mapping = new MappingImpl( config, virtual, physical, null, Config.INSPECT_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED,
							ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, false, true, true, false, false, null, -1, -1 );
					mappings.add( mapping );
				}
			}
			catch ( Exception e ) {
				// Log and skip invalid mappings
				Log log = config.getLog( "application" );
				if ( log != null ) {
					log.error( "VirtualDirectoryManager", "Failed to create virtual directory mapping: " + virtual + " -> " + physical, e );
				}
			}
		}

		return mappings.isEmpty() ? null : mappings.toArray( new Mapping[0] );
	}

	/**
	 * Clears the cache. Called when configuration changes or for testing.
	 */
	public void clearCache() {
		cache.clear();
	}

	/**
	 * Returns true if virtual directory feature is enabled (shared key is configured).
	 */
	public boolean isEnabled() {
		return !StringUtil.isEmpty( CONFIGURED_SHARED_KEY );
	}
}
