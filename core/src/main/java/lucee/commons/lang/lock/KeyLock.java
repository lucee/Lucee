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
package lucee.commons.lang.lock;

public final class KeyLock {

	private final Token token = new Token();
	private KeyLockListener listener;

	public KeyLock() {
		this.listener = NullKeyLockListener.getInstance();
	}

	public KeyLock(KeyLockListener listener) {
		this.listener = listener;
	}

	public void start(String key) {
		while (true) {
			// nobody inside

			synchronized (token) {
				if (token.value == null) {
					token.value = key;
					token.count++;
					listener.onStart(token.value, true);
					return;
				}
				if (key.equalsIgnoreCase(token.value)) {
					token.count++;
					listener.onStart(token.value, false);
					return;
				}
				try {
					token.wait();
				}
				catch (InterruptedException e) {
				}
			}
		}
	}

	public void end() {
		synchronized (token) {
			if (--token.count <= 0) {
				listener.onEnd(token.value, true);
				if (token.count < 0) token.count = 0;
				token.value = null;
			}
			else listener.onEnd(token.value, false);
			token.notify();
		}
	}

	public void setListener(KeyLockListener listener) {
		this.listener = listener;
	}

}

class Token {
	int count = 0;
	String value = null;
}