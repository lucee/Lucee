component extends="org.lucee.cfml.test.LuceeTestCase" labels="imap" {
	variables.isSupported = false;
	variables.imapCfg = getCredentials();
	if(!structIsEmpty(imapCfg))
		variables.isSupported=true;

	function run( testResults , testBox ) {
		describe( title="Test suite for CFIMAP Actions",  body=function() {
			describe(title="checking cfimap tag with secure access",
					skip=isNotSupported(!variables.isSupported),
					body = function( currentSpec ) {

				it(title="Checking cfimap action = 'ListAllFolders' ", body = function( currentSpec ) {
					var result = ListAllFolders("Inbox", "PORT_SECURE");
					expect( result.recordcount ).toBe( 1 );
				});

				it(title="Checking cfimap action = 'getHeaderOnly' ", body = function( currentSpec ) {
					cfimap(
						action = "getHeaderOnly",
						server = imapCfg.SERVER,
						port = imapCfg.PORT_SECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
						secure = true,
						name = "local.messages"
					);
					//systemOutput("-------getHeaderOnly", true);
					//systemOutput(local.messages, true);
					//systemOutput(local.messages.columnList, true);
					//systemOutput("", true);
					// query column checks for LDEV-4115
					var cols= "DATE,FROM,MESSAGENUMBER,MESSAGEID,REPLYTO,SUBJECT,CC,TO,SIZE,HEADER,UID";
					loop list=cols item="local.col" {
						expect ( queryColumnExists( messages, col ) ).toBeTrue( col );
					}
					expect ( listLen( messages.columnList ) ).toBe( listLen( cols ) );
					expect( messages.recordcount ).toBe( 0 );
				});

				it(title="Checking cfimap action = 'getAll' ", body = function( currentSpec ) {
					cfimap(
						action = "getAll",
						server = imapCfg.SERVER,
						port = imapCfg.PORT_SECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
						secure = true,
						name = "local.messages"
					);

					//systemOutput("----------getAll", true);
					//systemOutput(local.messages, true);
					//systemOutput(local.messages.columnList, true);
					//systemOutput("", true);
					// query column checks for LDEV-4115
					var cols= "DATE,FROM,MESSAGENUMBER,MESSAGEID,REPLYTO,SUBJECT,CC,TO,"
						& "SIZE,HEADER,UID,BODY,TEXTBODY,HTMLBODY,ATTACHMENTS,ATTACHMENTFILES,CIDS";

					loop list=cols item="local.col" {
						expect ( queryColumnExists( messages, col ) ).toBeTrue( col );
					}
					expect ( listLen( messages.columnList ) ).toBe( listLen( cols ) );
					expect( messages.recordcount ).toBe(0);
				});

				it(title="Checking cfimap action = 'CreateFolder' ", body = function( currentSpec ) {
					try{
						cfimap(
							action = "DeleteFolder",
							folder="NewFolderFromIMAP123",
							server = imapCfg.SERVER,
							port = imapCfg.PORT_SECURE,
							username = imapCfg.USERNAME,
							password = imapCfg.PASSWORD,
							secure = true
						);
					}catch(ee) {}

					cfimap(
						action = "CreateFolder",
						server = imapCfg.SERVER,
						port = imapCfg.PORT_SECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
						secure = true,
						folder = "NewFolderFromIMAP123"
					);

					var result = ListAllFolders("NewFolderFromIMAP123", "PORT_SECURE");
					expect( result.recordcount ).toBe(1);
				});

				it(title="Checking cfimap action = 'RenameFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "RenameFolder",
						folder="NewFolderFromIMAP123",
						newFolder="RenameFolderFromIMAP",
						server = imapCfg.SERVER,
						port = imapCfg.PORT_SECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
						secure = true
					);

					var result = ListAllFolders("RenameFolderFromIMAP", "PORT_SECURE");
					expect( result.recordcount ).toBe( 1 );
				});

				it(title="Checking cfimap action = 'DeleteFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "DeleteFolder",
						folder="RenameFolderFromIMAP",
						server = imapCfg.SERVER,
						port = imapCfg.PORT_SECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
						secure = true
					);

					var result = ListAllFolders("RenameFolderFromIMAP", "PORT_SECURE");
					expect( result.recordcount ).toBe( 0 );
				});

				it(title="Checking cfimap action = 'open' ", body = function( currentSpec ) {
					cfimap(
						action = "open",
						connection="openConnc",
						server = imapCfg.SERVER,
						port = imapCfg.PORT_SECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
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
					expect( result.RecordCount ).toBe(1);
				});

				it(title="Checking cfimap action = 'close' ", body = function( currentSpec ) {
					cfimap(
						action = "close",
						connection="openConnc"
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
							server = imapCfg.SERVER,
							port = imapCfg.PORT_SECURE,
							username = imapCfg.USERNAME,
							password = imapCfg.PASSWORD,
							secure = true
						);
					} catch ( any e ){
						var result = e.message;
					}
					expect(result).toBe('');
				});

				it(title="Checking cfimap with a specific syntax", body = function( currentSpec ) {

					imap
						action="open"
						server = imapCfg.SERVER
						username = imapCfg.USERNAME
						port = imapCfg.PORT_INSECURE
						secure="no"
						password = imapCfg.PASSWORD
						connection = "newsmasterbm";

					imap name="local.MyFolders" action="listallfolders" connection="newsmasterbm";

					 listfindnocase(ValueList(MyFolders.Name),"bm");
				});

			});

			describe(title="checking cfimap tag without secure access", skip=isNotSupported(!variables.isSupported),
					body = function( currentSpec ) {

				it(title="Checking cfimap action = 'ListAllFolders' ", body = function( currentSpec ) {
					var result = ListAllFolders("Inbox", "PORT_INSECURE");
					expect( result.recordcount ).toBe( 1 );
				});

				it(title="Checking cfimap action = 'CreateFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "CreateFolder",
						server = imapCfg.SERVER,
						folder = "NewFolderFromIMAP123"
						port = imapCfg.PORT_INSECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
						secure = false
					);

					var result = ListAllFolders("NewFolderFromIMAP123", "PORT_INSECURE");
					expect( result.recordcount ).toBe( 1 );
				});

				it(title="Checking cfimap action = 'RenameFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "RenameFolder",
						folder="NewFolderFromIMAP123"
						newFolder="RenameFolderFromIMAP",
						server = imapCfg.SERVER,
						port = imapCfg.PORT_INSECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
						secure = false
					);

					var result = ListAllFolders("RenameFolderFromIMAP", "PORT_INSECURE");
					expect( result.recordcount ).toBe( 1 );
				});

				it(title="Checking cfimap action = 'DeleteFolder' ", body = function( currentSpec ) {
					cfimap(
						action = "DeleteFolder",
						folder="RenameFolderFromIMAP"
						server = imapCfg.SERVER,
						port = imapCfg.PORT_INSECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
						secure = false
					);

					var result = ListAllFolders("RenameFolderFromIMAP", "PORT_INSECURE");
					expect( result.recordcount ).toBe( 0 );
				});

				it(title="Checking cfimap action = 'open' ", body = function( currentSpec ) {
					cfimap(
						action = "open",
						connection="openConnc",
						server = imapCfg.SERVER,
						port = imapCfg.PORT_INSECURE,
						username = imapCfg.USERNAME,
						password = imapCfg.PASSWORD,
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
							server = imapCfg.SERVER,
							port = imapCfg.PORT_INSECURE,
							username = imapCfg.USERNAME,
							password = imapCfg.PASSWORD,
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

	private query function ListAllFolders(string a1, string port){
		cfimap(
			action = "ListAllFolders",
			server = imapCfg.SERVER,
			port = imapCfg.port_secure,
			username = imapCfg.USERNAME,
			password = imapCfg.PASSWORD,
			secure = true,
			name = "local.Folder"
		);
		query name="local.result" dbtype="query"{
			echo("SELECT * FROM local.Folder WHERE fullname = '#arguments.a1#' ");
		}
		return local.result;
	}

	private struct function getCredentials(){
		return server.getTestService("imap");
	}
}
