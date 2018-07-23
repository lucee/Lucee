component extends="org.lucee.cfml.test.LuceeTestCase"{
	variables.isSupported = false;
	variables.imapSettings = getCredentials();
	if(!structIsEmpty(imapSettings))
		variables.isSupported=true;

	function run( testResults , testBox ) {
		describe( title="Test suite for CFIMAP Actions",  body=function() {
			describe(title="checking cfimap tag with secure access", skip=isNotSupported(!variables.isSupported), body = function( currentSpec ) {
				it(title="Checking cfimap action = 'ListAllFolders' ", body = function( currentSpec ) {
					var result = ListAllFolders("Inbox", "SECUREPORT");
					expect(result).toBe(1);
				});

				it(title="Checking cfimap action = 'CreateFolder' ", body = function( currentSpec ) {
					
					try{
						cfimap(
							action = "DeleteFolder",
							folder="NewFolderFromIMAP123",
							server = "#imapSettings.Imap.SERVER#",
							port = "#imapSettings.Imap.SECUREPORT#",
							username = "#imapSettings.USERNAME#",
							password = "#imapSettings.PASSWORD#",
							secure = true
						);
					}catch(ee) {}

					cfimap(
						action = "CreateFolder",
						server = "#imapSettings.Imap.SERVER#",
						port = "#imapSettings.Imap.SECUREPORT#",
						username = "#imapSettings.USERNAME#",
						password = "#imapSettings.PASSWORD#",
						secure = true,
						folder = "NewFolderFromIMAP123"
					);

					var result = ListAllFolders("NewFolderFromIMAP123", "SECUREPORT");
					expect(result).toBe(1);
				});

				it(title="Checking cfimap action = 'RenameFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "RenameFolder",
						folder="NewFolderFromIMAP123",
						newFolder="RenameFolderFromIMAP",
						server = "#imapSettings.Imap.SERVER#",
						port = "#imapSettings.Imap.SECUREPORT#",
						username = "#imapSettings.USERNAME#",
						password = "#imapSettings.PASSWORD#",
						secure = true
					);

					var result = ListAllFolders("RenameFolderFromIMAP", "SECUREPORT");
					expect(result).toBe(1);
				});

				it(title="Checking cfimap action = 'DeleteFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "DeleteFolder",
						folder="RenameFolderFromIMAP",
						server = "#imapSettings.Imap.SERVER#",
						port = "#imapSettings.Imap.SECUREPORT#",
						username = "#imapSettings.USERNAME#",
						password = "#imapSettings.PASSWORD#",
						secure = true
					);

					var result = ListAllFolders("RenameFolderFromIMAP", "SECUREPORT");
					expect(result).toBe(0);
				});

				it(title="Checking cfimap action = 'open' ", body = function( currentSpec ) {
					cfimap(
						action = "open",
						connection="openConnc",
						server = "#imapSettings.Imap.SERVER#",
						port = "#imapSettings.Imap.SECUREPORT#",
						username = "#imapSettings.USERNAME#",
						password = "#imapSettings.PASSWORD#",
						secure = true
					);

					cfimap(
						action = "ListAllFolders",
						connection="openConnc",
						name = "local.Folder"
					);
					query name="local.result" dbtype="query"{
						echo("SELECT * FROM local.Folder WHERE fullname = 'Inbox' ");
					}
					expect(result.RecordCount).toBe(1);
				});

				it(title="Checking cfimap action = 'close' ", body = function( currentSpec ) {
					cfimap(
						action = "close",
						connection="openConnc",
					);
					var result = "";
					try {
						cfimap(
							action = "ListAllFolders",
							connection="openConnc",
							name = "local.Folder"
						);
						query name="local.result" dbtype="query"{
							echo("SELECT * FROM local.Folder WHERE fullname = 'Inbox' ");
						}
					} catch ( any e ){
						var result = e.message;
					}
					expect(result).toBe('there is no connection available with name [openConnc]');
				});

				it(title="Checking cfimap action = 'Mark Read' ", body = function( currentSpec ) {
					var result = "";
					try {
						cfimap(
							action = "MarkRead",
							server = "#imapSettings.Imap.SERVER#",
							port = "#imapSettings.Imap.SECUREPORT#",
							username = "#imapSettings.USERNAME#",
							password = "#imapSettings.PASSWORD#",
							secure = true
						);
					} catch ( any e ){
						var result = e.message;
					}
					expect(result).toBe('');
				});

				it(title="Checking cfimap  with a specific syntax", body = function( currentSpec ) {
					
					imap 
						action="open" 
						server = imapSettings.Imap.SERVER
						username = imapSettings.USERNAME 
						secure="no" 
						password = imapSettings.PASSWORD
						connection = "newsmasterbm";

					imap name="local.MyFolders" action="listallfolders" connection="newsmasterbm";

					 listfindnocase(ValueList(MyFolders.Name),"bm");
				});

			});

			describe(title="checking cfimap tag without secure access", skip=isNotSupported(!variables.isSupported), body = function( currentSpec ) {
				it(title="Checking cfimap action = 'ListAllFolders' ", body = function( currentSpec ) {
					var result = ListAllFolders("Inbox", "INSECUREPORT");
					expect(result).toBe(1);
				});

				it(title="Checking cfimap action = 'CreateFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "CreateFolder",
						server = "#imapSettings.Imap.SERVER#",
						folder = "NewFolderFromIMAP123"
						port = "#imapSettings.Imap.INSECUREPORT#",
						username = "#imapSettings.USERNAME#",
						password = "#imapSettings.PASSWORD#",
						secure = false
					);

					var result = ListAllFolders("NewFolderFromIMAP123", "INSECUREPORT");
					expect(result).toBe(1);
				});

				it(title="Checking cfimap action = 'RenameFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "RenameFolder",
						folder="NewFolderFromIMAP123"
						newFolder="RenameFolderFromIMAP",
						server = "#imapSettings.Imap.SERVER#",
						port = "#imapSettings.Imap.INSECUREPORT#",
						username = "#imapSettings.USERNAME#",
						password = "#imapSettings.PASSWORD#",
						secure = false
					);

					var result = ListAllFolders("RenameFolderFromIMAP", "INSECUREPORT");
					expect(result).toBe(1);
				});

				it(title="Checking cfimap action = 'DeleteFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "DeleteFolder",
						folder="RenameFolderFromIMAP"
						server = "#imapSettings.Imap.SERVER#",
						port = "#imapSettings.Imap.INSECUREPORT#",
						username = "#imapSettings.USERNAME#",
						password = "#imapSettings.PASSWORD#",
						secure = false
					);

					var result = ListAllFolders("RenameFolderFromIMAP", "INSECUREPORT");
					expect(result).toBe(0);
				});

				it(title="Checking cfimap action = 'open' ", body = function( currentSpec ) {
					cfimap(
						action = "open",
						connection="openConnc",
						server = "#imapSettings.Imap.SERVER#",
						port = "#imapSettings.Imap.INSECUREPORT#",
						username = "#imapSettings.USERNAME#",
						password = "#imapSettings.PASSWORD#",
						secure = false
					);

					cfimap(
						action = "ListAllFolders",
						connection="openConnc",
						name = "local.Folder"
					);
					query name="local.result" dbtype="query"{
						echo("SELECT * FROM local.Folder WHERE fullname = 'Inbox' ");
					}
					expect(result.RecordCount).toBe(1);
				});

				it(title="Checking cfimap action = 'close' ", body = function( currentSpec ) {
					cfimap(
						action = "close",
						connection="openConnc",
					);
					var result = "";
					try {
						cfimap(
							action = "ListAllFolders",
							connection="openConnc",
							name = "local.Folder"
						);
						query name="local.result" dbtype="query"{
							echo("SELECT * FROM local.Folder WHERE fullname = 'Inbox' ");
						}
					} catch ( any e ){
						var result = e.message;
					}
					expect(result).toBe('there is no connection available with name [openConnc]');
				});

				it(title="Checking cfimap action = 'Mark Read' ", body = function( currentSpec ) {
					var result = "";
					try {
						cfimap(
							action = "MarkRead",
							server = "#imapSettings.Imap.SERVER#",
							port = "#imapSettings.Imap.INSECUREPORT#",
							username = "#imapSettings.USERNAME#",
							password = "#imapSettings.PASSWORD#",
							secure = false
						);
					} catch ( any e ){
						var result = e.message;
					}
					expect(result).toBe('');
				});
			});
		});
	}

	// private functions
	private boolean function isNotSupported( required boolean s1 ) {
		return arguments.s1;
	}

	private string function ListAllFolders(string a1, string port){
		cfimap(
			action = "ListAllFolders",
			server = "#imapSettings.Imap.SERVER#",
			port = "#imapSettings.Imap[arguments.port]#",
			username = "#imapSettings.USERNAME#",
			password = "#imapSettings.PASSWORD#",
			secure = true,
			name = "local.Folder"
		);
		query name="local.result" dbtype="query"{
			echo("SELECT * FROM local.Folder WHERE fullname = '#arguments.a1#' ");
		}

		return local.result.RecordCount;
	}

	private struct function getCredentials(){
		var result={};

		if(isNull(server.system)){
			// for lucee 4.5
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MAIL_USERNAME) &&
			!isNull(server.system.environment.MAIL_PASSWORD) &&
			!isNull(server.system.environment.IMAP_SERVER) &&
			!isNull(server.system.environment.IMAP_PORT_SECURE) &&
			!isNull(server.system.environment.IMAP_PORT_INSECURE)
		){
			// getting the credentials from the environment variables
			var result={};
			// Common settings
			result.username=server.system.environment.MAIL_USERNAME;
			result.password=server.system.environment.MAIL_PASSWORD;
			// IMAP related settings
			result.IMAP.server=server.system.environment.IMAP_SERVER;
			result.IMAP.securePort=server.system.environment.IMAP_PORT_SECURE;
			result.IMAP.insecurePort=server.system.environment.IMAP_PORT_INSECURE;
		}else if(
			!isNull(server.system.properties.MAIL_USERNAME) &&
			!isNull(server.system.properties.MAIL_PASSWORD) &&
			!isNull(server.system.properties.IMAP_SERVER) &&
			!isNull(server.system.properties.IMAP_PORT_SECURE) &&
			!isNull(server.system.properties.IMAP_PORT_INSECURE)
		){
			// getting the credentials from the system properties
			var result={};
			// Common settings
			result.username=server.system.properties.MAIL_USERNAME;
			result.password=server.system.properties.MAIL_PASSWORD;
			// IMAP related settings
			result.IMAP.server=server.system.properties.IMAP_SERVER;
			result.IMAP.securePort=server.system.properties.IMAP_PORT_SECURE;
			result.IMAP.insecurePort=server.system.properties.IMAP_PORT_INSECURE;
		}

		return result;
	}
}
