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
package lucee.runtime.net.ntp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;

/**
 * NtpClient - an NTP client for Java. This program connects to an NTP server
 */
public final class NtpClient {

	private String serverName;

	/**
	 * default constructor of the class
	 * 
	 * @param serverName
	 */
	public NtpClient(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * returns the offest from the ntp server to local system
	 * 
	 * @return
	 * @throws IOException
	 */
	public long getOffset() throws IOException {
		/// Send request
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(20000);
			InetAddress address = InetAddress.getByName(serverName);
			byte[] buf = new NtpMessage().toByteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);

			// Set the transmit timestamp *just* before sending the packet
			NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);

			socket.send(packet);

			// Get response
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			// Immediately record the incoming timestamp
			double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

			// Process response
			NtpMessage msg = new NtpMessage(packet.getData());
			// double roundTripDelay = (destinationTimestamp-msg.originateTimestamp) -
			// (msg.receiveTimestamp-msg.transmitTimestamp);
			double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;

			return (long) (localClockOffset * 1000);
		}
		finally {
			IOUtil.close(socket);
		}
	}

	public long getOffset(long defaultValue) {
		try {
			return getOffset();
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
			return defaultValue;
		}

	}

	/**
	 * returns the current time from ntp server in ms from 1970
	 * 
	 * @return
	 * @throws IOException
	 */
	public long currentTimeMillis() throws IOException {
		return System.currentTimeMillis() + getOffset();
	}

	/*
	 * public static void main(String[] args) throws IOException{ NtpClient ntp=new
	 * NtpClient("time.nist.gov");
	 * 
	 * } public static void main(String[] args) throws IOException {
	 * 
	 * String serverName="time.nist.gov";
	 * 
	 * 
	 * 
	 * 
	 * /// Send request DatagramSocket socket = new DatagramSocket(); InetAddress address =
	 * InetAddress.getByName(serverName); byte[] buf = new NtpMessage().toByteArray(); DatagramPacket
	 * packet = new DatagramPacket(buf, buf.length, address, 123);
	 * 
	 * // Set the transmit timestamp *just* before sending the packet // ToDo: Does this improve
	 * performance or not? NtpMessage.encodeTimestamp(packet.getData(), 40,
	 * (System.currentTimeMillis()/1000.0) + 2208988800.0);
	 * 
	 * socket.send(packet);
	 * 
	 * // Get response packet = new DatagramPacket(buf, buf.length); socket.receive(packet);
	 * 
	 * // Immediately record the incoming timestamp double destinationTimestamp =
	 * (System.currentTimeMillis()/1000.0) + 2208988800.0;
	 * 
	 * 
	 * // Process response NtpMessage msg = new NtpMessage(packet.getData()); double roundTripDelay =
	 * (destinationTimestamp-msg.originateTimestamp) - (msg.receiveTimestamp-msg.transmitTimestamp);
	 * double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) +
	 * (msg.transmitTimestamp - destinationTimestamp)) / 2;
	 * 
	 * 
	 * // Display response
	 * 
	 * socket.close(); }
	 */

}