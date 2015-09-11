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
package lucee.commons.io.retirement;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.io.SystemUtil;

public class RetireOutputStreamFactory {
	
	static List<RetireOutputStream> list=new ArrayList<RetireOutputStream>();
	private static RetireThread thread;
	
	static void startThread(long timeout) {
		if(timeout<1000) timeout=1000;
		if(thread==null || !thread.isAlive()) {
			thread=new RetireThread(timeout);
			thread.start();
		}
		else if(thread.sleepTime>timeout) {
			thread.sleepTime=timeout;
			SystemUtil.notify(thread);
		}
	}

	static class RetireThread extends Thread {
		
		public long sleepTime;
		
		public RetireThread(long sleepTime){
			this.sleepTime=sleepTime;
		}
		
		
		@Override
		public void run(){
			//print.e("start thread");
			while(true){
				try{
					if(list.size()==0) break;
					SystemUtil.wait(this,sleepTime);
					//SystemUtil.sleep(sleepTime);
					RetireOutputStream[] arr = list.toArray(new RetireOutputStream[list.size()]); // not using iterator to avoid ConcurrentModificationException
					for(int i=0;i<arr.length;i++){
						arr[i].retire();
					}
					
				}
				catch(Throwable t){t.printStackTrace();}
			}
			//print.e("stop thread");
			thread=null;
		}
	}
}