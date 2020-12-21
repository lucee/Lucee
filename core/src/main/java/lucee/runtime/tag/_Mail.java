/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.tag;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.net.mail.MailClient;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

/**
 * Retrieves and deletes e-mail messages from a POP mail server.
 */
public abstract class _Mail extends TagImpl {

	public class Credential {

	}

	private String server;
	private int port = -1;

	private String username;
	private String password;
	private String action = "getheaderonly";
	private String name;
	private String[] messageNumber;
	private String[] uid;
	private Resource attachmentPath;
	private int timeout = 60;
	private int startrow = 1;
	private int maxrows = -1;
	private boolean generateUniqueFilenames = false;
	private boolean secure = false;
	private String folder;
	private String newfolder;
	private boolean recurse;
	private String connection;
	private String id;
	private List<Credential> credentials = new ArrayList<_Mail.Credential>();

	public _Mail() {
		this.id = CreateUniqueId.invoke();
	}

	@Override
	public void release() {
		server = null;
		port = -1;
		username = null;
		password = null;
		action = "getheaderonly";
		name = null;
		messageNumber = null;
		uid = null;
		attachmentPath = null;
		timeout = 60;
		startrow = 1;
		maxrows = -1;
		generateUniqueFilenames = false;
		secure = false;
		folder = null;
		newfolder = null;
		recurse = false;
		connection = null;
		super.release();
	}

	/**
	 * @param server The server to set.
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param port The port to set.
	 */
	public void setPort(double port) {
		this.port = (int) port;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public void setNewfolder(String newfolder) {
		this.newfolder = newfolder;
	}

	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isSecure() {
		return secure;
	}

	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param action The action to set.
	 */
	public void setAction(String action) {
		this.action = action.trim().toLowerCase();
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param messageNumber The messageNumber to set.
	 * @throws PageException
	 */
	public void setMessagenumber(String messageNumber) throws PageException {
		this.messageNumber = ArrayUtil.trim(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(messageNumber, ',')));
		if (this.messageNumber.length == 0) this.messageNumber = null;
	}

	/**
	 * @param uid The uid to set.
	 * @throws PageException
	 */
	public void setUid(String uid) throws PageException {
		this.uid = ArrayUtil.trim(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(uid, ',')));
		if (this.uid.length == 0) this.uid = null;
	}

	/**
	 * @param attachmentPath The attachmentPath to set.
	 * @throws PageException
	 */
	public void setAttachmentpath(String attachmentPath) throws PageException {
		// try {
		Resource attachmentDir = pageContext.getConfig().getResource(attachmentPath);
		if (!attachmentDir.exists() && !attachmentDir.mkdir()) {
			attachmentDir = pageContext.getConfig().getTempDirectory().getRealResource(attachmentPath);
			if (!attachmentDir.exists() && !attachmentDir.mkdir()) throw new ApplicationException("Directory [" + attachmentPath + "] doesn't exist and couldn't be created");
		}
		if (!attachmentDir.isDirectory()) throw new ApplicationException("File [" + attachmentPath + "] is not a directory");
		pageContext.getConfig().getSecurityManager().checkFileLocation(attachmentDir);
		this.attachmentPath = attachmentDir;
		/*
		 * } catch(IOException ioe) { throw Caster.toPageException(ioe); }
		 */
	}

	/**
	 * @param maxrows The maxrows to set.
	 */
	public void setMaxrows(double maxrows) {
		this.maxrows = (int) maxrows;
	}

	/**
	 * @param startrow The startrow to set.
	 */
	public void setStartrow(double startrow) {
		this.startrow = (int) startrow;
	}

	/**
	 * @param timeout The timeout to set.
	 */
	public void setTimeout(double timeout) {
		this.timeout = (int) timeout;
	}

	/**
	 * @param generateUniqueFilenames The generateUniqueFilenames to set.
	 */
	public void setGenerateuniquefilenames(boolean generateUniqueFilenames) {
		this.generateUniqueFilenames = generateUniqueFilenames;
	}

	/**
	 * @param debug The debug to set.
	 */
	public void setDebug(boolean debug) {
		// does nothing this.debug = debug;
	}

	@Override
	public int doStartTag() throws PageException {

		// check attrs
		if (port == -1) port = getDefaultPort();

		checkConnection();

		// PopClient client = new PopClient(server,port,username,password);
		MailClient client;
		try {
			client = MailClient.getInstance(getType(), server, port, username, password, secure, connection, id);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		// store connection data
		if (!StringUtil.isEmpty(connection) && StringUtil.isEmpty(server)) {

		}

		client.setTimeout(timeout * 1000);
		client.setMaxrows(maxrows);
		if (startrow > 1) client.setStartrow(startrow - 1);
		client.setUniqueFilenames(generateUniqueFilenames);
		if (attachmentPath != null) client.setAttachmentDirectory(attachmentPath);

		if (uid != null) messageNumber = null;

		try {
			// client.connect();

			if (action.equals("getheaderonly")) {
				required(getTagName(), action, "name", name);
				pageContext.setVariable(name, client.getMails(messageNumber, uid, false, folder));
			}
			else if (action.equals("getall")) {
				required(getTagName(), action, "name", name);
				pageContext.setVariable(name, client.getMails(messageNumber, uid, true, folder));
			}
			else if (action.equals("delete")) {
				client.deleteMails(messageNumber, uid);
			}

			// imap only
			else if (getType() == MailClient.TYPE_IMAP && action.equals("open")) {
				// no action necessary, because getting a client above already does the trick
			}
			else if (getType() == MailClient.TYPE_IMAP && action.equals("close")) {
				MailClient.removeInstance(client);
				// no action necessary, because getting a client above already does the trick
			}
			else if (getType() == MailClient.TYPE_IMAP && action.equals("markread")) {
				client.markRead(folder);
			}
			else if (getType() == MailClient.TYPE_IMAP && action.equals("createfolder")) {
				required(getTagName(), action, "folder", folder);
				client.createFolder(folder);
			}
			else if (getType() == MailClient.TYPE_IMAP && action.equals("deletefolder")) {
				required(getTagName(), action, "folder", folder);
				client.deleteFolder(folder);
			}
			else if (getType() == MailClient.TYPE_IMAP && action.equals("renamefolder")) {
				required(getTagName(), action, "folder", folder);
				required(getTagName(), action, "newfolder", newfolder);
				client.renameFolder(folder, newfolder);
			}
			else if (getType() == MailClient.TYPE_IMAP && action.equals("listallfolders")) {
				pageContext.setVariable(name, client.listAllFolder(folder, recurse, startrow, maxrows));
			}
			else if (getType() == MailClient.TYPE_IMAP && action.equals("movemail")) {
				required(getTagName(), action, "newfolder", newfolder);
				client.moveMail(folder, newfolder, messageNumber, uid);
			}
			else {
				String actions = "getHeaderOnly,getAll,delete";
				if (getType() == MailClient.TYPE_IMAP) actions += "open,close,markread,createfolder,deletefolder,renamefolder,listallfolders,movemail";

				throw new ApplicationException("Invalid value for attribute [action], valid values are [" + actions + "]");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			// client.disconnectEL();
		}
		return SKIP_BODY;
	}

	private void checkConnection() throws ApplicationException {
		if (StringUtil.isEmpty(connection) && StringUtil.isEmpty(server)) {
			throw new ApplicationException("You need to define the attribute [connection] or [server].");
		}
	}

	protected abstract int getType();

	protected abstract int getDefaultPort();

	protected abstract String getTagName();
}
