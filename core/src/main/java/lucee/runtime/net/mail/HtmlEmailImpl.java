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
package lucee.runtime.net.mail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import lucee.commons.lang.StringUtil;

/**
 * An HTML multipart email.
 *
 * <p>
 * This class is used to send HTML formatted email. A text message can also be set for HTML unaware
 * email clients, such as text-based email clients.
 *
 * <p>
 * This class also inherits from MultiPartEmail, so it is easy to add attachments to the email.
 *
 * <p>
 * To send an email in HTML, one should create a HtmlEmail, then use the setFrom, addTo, etc.
 * methods. The HTML content can be set with the setHtmlMsg method. The alternative text content can
 * be set with setTextMsg.
 *
 * <p>
 * Either the text or HTML can be omitted, in which case the "main" part of the multipart becomes
 * whichever is supplied rather than a multipart/alternative.
 *
 *
 */
public final class HtmlEmailImpl extends MultiPartEmail {
	/** Definition of the length of generated CID's */
	public static final int CID_LENGTH = 10;

	/**
	 * Text part of the message. This will be used as alternative text if the email client does not
	 * support HTML messages.
	 */
	protected String text;

	/** Html part of the message */
	protected String html;

	/** Embedded images */
	protected List inlineImages = new ArrayList();

	/**
	 * Set the text content.
	 *
	 * @param aText A String.
	 * @return An HtmlEmail.
	 * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
	 *
	 */
	public HtmlEmailImpl setTextMsg(String aText) throws EmailException {
		if (StringUtil.isEmpty(aText)) {
			throw new EmailException("Invalid message supplied");
		}
		this.text = aText;
		return this;
	}

	/**
	 * Set the HTML content.
	 *
	 * @param aHtml A String.
	 * @return An HtmlEmail.
	 * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
	 *
	 */
	public HtmlEmailImpl setHtmlMsg(String aHtml) throws EmailException {
		if (StringUtil.isEmpty(aHtml)) {
			throw new EmailException("Invalid message supplied");
		}
		this.html = aHtml;
		return this;
	}

	/**
	 * Set the message.
	 *
	 * <p>
	 * This method overrides the MultiPartEmail setMsg() method in order to send an HTML message instead
	 * of a full text message in the mail body. The message is formatted in HTML for the HTML part of
	 * the message, it is let as is in the alternate text part.
	 *
	 * @param msg A String.
	 * @return An Email.
	 * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
	 *
	 */
	@Override
	public Email setMsg(String msg) throws EmailException {
		if (StringUtil.isEmpty(msg)) {
			throw new EmailException("Invalid message supplied");
		}

		setTextMsg(msg);

		setHtmlMsg(new StringBuffer().append("<html><body><pre>").append(msg).append("</pre></body></html>").toString());

		return this;
	}

	/**
	 * Embeds an URL in the HTML.
	 *
	 * <p>
	 * This method allows to embed a file located by an URL into the mail body. It allows, for instance,
	 * to add inline images to the email. Inline files may be referenced with a <code>cid:xxxxxx</code>
	 * URL, where xxxxxx is the Content-ID returned by the embed function.
	 *
	 * <p>
	 * Example of use:<br>
	 * <code><pre>
	 * HtmlEmail he = new HtmlEmail();
	 * he.setHtmlMsg("&lt;html&gt;&lt;img src=cid:" +
	 *  embed("file:/my/image.gif","image.gif") +
	 *  "&gt;&lt;/html&gt;");
	 * // code to set the others email fields (not shown)
	 * </pre></code>
	 *
	 * @param url The URL of the file.
	 * @param cid A String with the Content-ID of the file.
	 * @param name The name that will be set in the filename header field.
	 * @throws EmailException when URL supplied is invalid also see javax.mail.internet.MimeBodyPart for
	 *             definitions
	 *
	 */
	public void embed(URL url, String cid, String name) throws EmailException {
		// verify that the URL is valid
		try {
			InputStream is = url.openStream();
			is.close();
		}
		catch (IOException e) {
			throw new EmailException("Invalid URL");
		}

		MimeBodyPart mbp = new MimeBodyPart();

		try {
			mbp.setDataHandler(new DataHandler(new URLDataSource(url)));
			mbp.setFileName(name);
			mbp.setDisposition("inline");
			mbp.addHeader("Content-ID", "<" + cid + ">");
			this.inlineImages.add(mbp);
		}
		catch (MessagingException me) {
			throw new EmailException(me);
		}
	}

	/**
	 * Does the work of actually building the email.
	 *
	 * @exception EmailException if there was an error.
	 *
	 */
	@Override
	public void buildMimeMessage() throws EmailException {
		try {
			// if the email has attachments then the base type is mixed,
			// otherwise it should be related
			if (this.isBoolHasAttachments()) {
				this.buildAttachments();
			}
			else {
				this.buildNoAttachments();
			}

		}
		catch (MessagingException me) {
			throw new EmailException(me);
		}
		super.buildMimeMessage();
	}

	/**
	 * @throws EmailException EmailException
	 * @throws MessagingException MessagingException
	 */
	private void buildAttachments() throws MessagingException, EmailException {
		MimeMultipart container = this.getContainer();
		MimeMultipart subContainer = null;
		MimeMultipart subContainerHTML = new MimeMultipart("related");
		BodyPart msgHtml = null;
		BodyPart msgText = null;

		container.setSubType("mixed");
		subContainer = new MimeMultipart("alternative");

		if (!StringUtil.isEmpty(this.text)) {
			msgText = new MimeBodyPart();
			subContainer.addBodyPart(msgText);

			if (!StringUtil.isEmpty(this.charset)) {
				msgText.setContent(this.text, Email.TEXT_PLAIN + "; charset=" + this.charset);
			}
			else {
				msgText.setContent(this.text, Email.TEXT_PLAIN);
			}
		}

		if (!StringUtil.isEmpty(this.html)) {
			if (this.inlineImages.size() > 0) {
				msgHtml = new MimeBodyPart();
				subContainerHTML.addBodyPart(msgHtml);
			}
			else {
				msgHtml = new MimeBodyPart();
				subContainer.addBodyPart(msgHtml);
			}

			if (!StringUtil.isEmpty(this.charset)) {
				msgHtml.setContent(this.html, Email.TEXT_HTML + "; charset=" + this.charset);
			}
			else {
				msgHtml.setContent(this.html, Email.TEXT_HTML);
			}

			Iterator iter = this.inlineImages.iterator();
			while (iter.hasNext()) {
				subContainerHTML.addBodyPart((BodyPart) iter.next());
			}
		}

		// add sub containers to message
		this.addPart(subContainer, 0);

		if (this.inlineImages.size() > 0) {
			// add sub container to message
			this.addPart(subContainerHTML, 1);
		}
	}

	/**
	 * @throws EmailException EmailException
	 * @throws MessagingException MessagingException
	 */
	private void buildNoAttachments() throws MessagingException, EmailException {
		MimeMultipart container = this.getContainer();
		MimeMultipart subContainerHTML = new MimeMultipart("related");

		container.setSubType("alternative");

		BodyPart msgText = null;
		BodyPart msgHtml = null;

		if (!StringUtil.isEmpty(this.text)) {
			msgText = this.getPrimaryBodyPart();
			if (!StringUtil.isEmpty(this.charset)) {
				msgText.setContent(this.text, Email.TEXT_PLAIN + "; charset=" + this.charset);
			}
			else {
				msgText.setContent(this.text, Email.TEXT_PLAIN);
			}
		}

		if (!StringUtil.isEmpty(this.html)) {
			// if the txt part of the message was null, then the html part
			// will become the primary body part
			if (msgText == null) {
				msgHtml = getPrimaryBodyPart();
			}
			else {
				if (this.inlineImages.size() > 0) {
					msgHtml = new MimeBodyPart();
					subContainerHTML.addBodyPart(msgHtml);
				}
				else {
					msgHtml = new MimeBodyPart();
					container.addBodyPart(msgHtml, 1);
				}
			}

			if (!StringUtil.isEmpty(this.charset)) {
				msgHtml.setContent(this.html, Email.TEXT_HTML + "; charset=" + this.charset);
			}
			else {
				msgHtml.setContent(this.html, Email.TEXT_HTML);
			}

			Iterator iter = this.inlineImages.iterator();
			while (iter.hasNext()) {
				subContainerHTML.addBodyPart((BodyPart) iter.next());
			}

			if (this.inlineImages.size() > 0) {
				// add sub container to message
				this.addPart(subContainerHTML);
			}
		}
	}
}