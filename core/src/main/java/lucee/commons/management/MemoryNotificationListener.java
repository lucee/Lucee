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
package lucee.commons.management;

import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryType;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeDataSupport;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.config.Config;

public class MemoryNotificationListener implements NotificationListener {

	private Map<String, MemoryType> types;

	public MemoryNotificationListener(Map<String, MemoryType> types) {
		this.types = types;
	}

	@Override
	public void handleNotification(Notification not, Object handback) {

		if (not.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
			CompositeDataSupport data = (CompositeDataSupport) not.getUserData();

			String poolName = (String) data.get("poolName");
			MemoryType type = types.get(poolName);
			if (type == MemoryType.HEAP) {
				// clear heap
				LogUtil.log(Log.LEVEL_INFO, MemoryNotificationListener.class.getName(), "Clear heap!");
			}
			else if (type == MemoryType.NON_HEAP) {
				// clear none-heap
				((Config) handback).checkPermGenSpace(false);
			}

		}
	}
}