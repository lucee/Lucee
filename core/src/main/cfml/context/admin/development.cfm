<!---pre>
security
- file (rechte vergeben
	- none (kein Zugriff auf FS)
	- local (nur lokalen Zugriff auf FS; also alles oberhalb webroot dir ?was ist wenn mapping ausserhalb)
	- all (volles schreibrecht)
- java
	- none (kann java reflexion nicht nutzen Bsp: now().getTime() )
	- all darf ohne einschraenkung
settings
- regional (Standarteinstellung fuer die lokaladmins)
- component  (Standarteinstellung fuer die lokaladmins)
- scope (Standarteinstellung fuer die lokaladmins)

global services
- datasource (DS die allen zur verfuegung stehen)
- Mail (Standarteinstellung fuer die lokaladmins)

trace
	(was geht ab, uberischt laufender request, memory usage, 
	hier sollten auch logger eingeschaltet werden koennen)
</pre>--->

<cfset contextes=admin.contextes>

<cfoutput>
<cfloop collection="#contextes#" item="key">
	<cfset con=contextes[key]>
	<cfset _config=contextes[key].config>
	key:#key#<br>
	<cfdump var="#con.engineInfo.specificationVersion#">
	<cfdump var="#_config.servletContext#">
	<cfdump var="#_config#">
	<cfdump var="#con#">
	<br>
	<cfbreak>
</cfloop>
</cfoutput>
