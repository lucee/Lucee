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

			/*
			 * CompositeDataSupport usage=(CompositeDataSupport) data.get("usage"); print.e(poolName);
			 * print.e(types.get(poolName)); print.e(data.get("count"));
			 * 
			 * print.e(usage.get("committed")); print.e(usage.get("init")); print.e(usage.get("max"));
			 * print.e(usage.get("used"));
			 * 
			 * long max=Caster.toLongValue(usage.get("max"),0); long
			 * used=Caster.toLongValue(usage.get("used"),0); long free=max-used; print.o("m:"+max);
			 * print.o("f:"+free); print.o("%:"+(100L*used/max)); //not.
			 */
		}
		/*
		 * javax.management.openmbean.CompositeDataSupport(
		 * compositeType=javax.management.openmbean.CompositeType( name=java.lang.management.MemoryUsage,
		 * items=( (itemName=committed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),
		 * (itemName=init,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),
		 * (itemName=max,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),
		 * (itemName=used,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={
		 * committed=101580800, init=65404928, max=110362624, used=101085960})
		 * 
		 * 
		 * 
		 * javax.management.openmbean.CompositeDataSupport(
		 * compositeType=javax.management.openmbean.CompositeType(
		 * name=java.lang.management.MemoryNotificationInfo, items=(
		 * (itemName=count,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),
		 * (itemName=poolName,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),
		 * (itemName=usage,itemType=javax.management.openmbean.CompositeType(name=java.lang.management.
		 * MemoryUsage,items=((itemName=committed,itemType=javax.management.openmbean.SimpleType(name=java.
		 * lang.Long)),(itemName=init,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(
		 * itemName=max,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=used,
		 * itemType=javax.management.openmbean.SimpleType(name=java.lang.Long))))))),contents={count=1,
		 * poolName=CMS Old Gen,
		 * usage=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.
		 * CompositeType(name=java.lang.management.MemoryUsage,items=((itemName=committed,itemType=javax.
		 * management.openmbean.SimpleType(name=java.lang.Long)),(itemName=init,itemType=javax.management.
		 * openmbean.SimpleType(name=java.lang.Long)),(itemName=max,itemType=javax.management.openmbean.
		 * SimpleType(name=java.lang.Long)),(itemName=used,itemType=javax.management.openmbean.SimpleType(
		 * name=java.lang.Long)))),contents={committed=101580800, init=65404928, max=110362624,
		 * used=101085944})})
		 * 
		 */
		/*
		 * print.e(data.getCompositeType()); print.e(not.getSource().getClass().getName());
		 * print.e(not.getSource()); ObjectName on=(ObjectName) not.getSource();
		 * print.e(on.getKeyPropertyList());
		 */

		/*
		 * print.e(not.getUserData().getClass().getName()); print.e(not.getUserData());
		 * 
		 * print.e(not.getMessage()); print.e(not.getSequenceNumber()); print.e(not.getTimeStamp());
		 * print.e(not.getType());
		 */
	}

}