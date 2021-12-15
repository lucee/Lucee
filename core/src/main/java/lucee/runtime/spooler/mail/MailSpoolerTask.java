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
package lucee.runtime.spooler.mail;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.InternetAddress;

import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.mail.MailException;
import lucee.runtime.net.mail.MailUtil;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.smtp.SMTPClient;
import lucee.runtime.op.Caster;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.ExecutionPlanImpl;
import lucee.runtime.spooler.SpoolerTaskListener;
import lucee.runtime.spooler.SpoolerTaskSupport;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;

public class MailSpoolerTask extends SpoolerTaskSupport {
	private static final ExecutionPlan[] EXECUTION_PLANS = new ExecutionPlan[] { new ExecutionPlanImpl(1, 60), new ExecutionPlanImpl(1, 5 * 60), new ExecutionPlanImpl(1, 3600),
			new ExecutionPlanImpl(2, 24 * 3600), };

	private static final Key CC = KeyImpl.init("cc");
	private static final Key BCC = KeyImpl.init("bcc");

	private static final Key FAILTO = KeyImpl.init("failto");
	private static final Key REPLYTO = KeyImpl.init("replyto");

	private SMTPClient client;
	private Server[] servers;
	private SpoolerTaskListener listener;

	private MailSpoolerTask(ExecutionPlan[] plans, SMTPClient client, Server[] servers, long sendTime) {
		super(plans, sendTime);
		this.client = client;
		this.servers = servers;
	}

	public MailSpoolerTask(SMTPClient client, Server[] servers, long sendTime) {
		this(EXECUTION_PLANS, client, servers, sendTime);
	}

	@Override
	public String getType() {
		return "mail";
	}

	@Override
	public String subject() {
		return client.getSubject();
	}

	@Override
	public Struct detail() {
		StructImpl sct = new StructImpl();
		sct.setEL("subject", client.getSubject());

		if (client.hasHTMLText()) sct.setEL("body", StringUtil.max(client.getHTMLTextAsString(), 1024, "..."));
		else if (client.hasPlainText()) sct.setEL("body", StringUtil.max(client.getPlainTextAsString(), 1024, "..."));

		sct.setEL("from", toString(client.getFrom()));

		InternetAddress[] adresses = client.getTos();
		sct.setEL("to", toString(adresses));

		adresses = client.getCcs();
		if (!ArrayUtil.isEmpty(adresses)) sct.setEL("cc", toString(adresses));

		adresses = client.getBccs();
		if (!ArrayUtil.isEmpty(adresses)) sct.setEL("bcc", toString(adresses));

		return sct;
	}

	private static String toString(InternetAddress[] adresses) {
		if (adresses == null) return "";

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < adresses.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(toString(adresses[i]));
		}
		return sb.toString();
	}

	private static String toString(InternetAddress address) {
		if (address == null) return "";
		String addr = address.getAddress();
		String per = address.getPersonal();
		if (StringUtil.isEmpty(per)) return addr;
		if (StringUtil.isEmpty(addr)) return per;

		return per + " (" + addr + ")";
	}

	@Override
	public Object execute(Config config) throws PageException {
		try {
			client._send((ConfigWeb) config, servers);
		}
		catch (MailException e) {
			throw Caster.toPageException(e);
		}
		return null;
	}

	@Override
	public SpoolerTaskListener getListener() {
		return listener;
	}

	public void setListener(SpoolerTaskListener listener) {
		this.listener = listener;
	}

	public void mod(Struct sct) throws UnsupportedEncodingException, PageException, MailException {
		// MUST more

		// FROM
		Object o = sct.get(KeyConstants._from, null);
		if (o != null) client.setFrom(MailUtil.toInternetAddress(o));

		// TO
		o = sct.get(KeyConstants._to, null);
		if (o != null) client.setTos(MailUtil.toInternetAddresses(o));

		// BCC
		o = sct.get(BCC, null);
		if (o != null) client.setBCCs(MailUtil.toInternetAddresses(o));

		// replyto
		o = sct.get(FAILTO, null);
		if (o != null) client.setFailTos(MailUtil.toInternetAddresses(o));

		// replyto
		o = sct.get(REPLYTO, null);
		if (o != null) client.setReplyTos(MailUtil.toInternetAddresses(o));

		// subject
		o = sct.get(KeyConstants._subject, null);
		if (o != null) client.setSubject(StringUtil.collapseWhitespace(Caster.toString(o)));

	}

}