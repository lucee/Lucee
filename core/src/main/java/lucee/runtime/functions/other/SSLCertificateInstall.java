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
package lucee.runtime.functions.other;

import lucee.commons.io.res.Resource;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.CertificateInstaller;
import lucee.runtime.op.Caster;

public final class SSLCertificateInstall implements Function {

	private static final long serialVersionUID = -831759073098524176L;

	public static String call( PageContext pc, String host ) throws PageException {
		return call( pc, host, 443 );
	}

	public static String call( PageContext pc, String host, Number port ) throws PageException {
		return call( pc, host, port, null, null );
	}

	public static String call( PageContext pc, String host, Number port, Object cacerts ) throws PageException {
		return call( pc, host, port, cacerts, null );
	}

	public static String call( PageContext pc, String host, Number port, Object cacerts, String password ) throws PageException {
		try {
			if ( cacerts == null ) {
				CertificateInstaller.installToCustomCaCerts( host, Caster.toIntValue( port ) );
			}
			else {
				Resource keystore = Caster.toResource( pc, cacerts, true );
				CertificateInstaller installer = password == null
					? new CertificateInstaller( keystore, host, Caster.toIntValue( port ) )
					: new CertificateInstaller( keystore, host, Caster.toIntValue( port ), password.toCharArray() );
				installer.installAll( true );
			}
			HTTPEngine4Impl.releaseConnectionManager();
		}
		catch ( Exception e ) {
			throw Caster.toPageException( e );
		}
		return "";
	}

}