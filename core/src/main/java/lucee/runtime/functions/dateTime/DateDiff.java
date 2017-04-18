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
/**
 * Implements the CFML Function datediff
 */
package lucee.runtime.functions.dateTime;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.JREDateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.dt.DateTime;

/**
 * 
 */
public final class DateDiff extends BIF {

	private static final long serialVersionUID = 4243793930337910884L;

	//private static final int DATEPART_S = 0;
	//private static final int DATEPART_N = 1;
	//private static final int DATEPART_H = 2;
	private static final int DATEPART_D = 3;
	private static final int DATEPART_Y = DATEPART_D;
	private static final int DATEPART_YYYY = 10;
	private static final int DATEPART_M = 11;
	private static final int DATEPART_W = 12;
	private static final int DATEPART_WW = DATEPART_W;
	private static final int DATEPART_Q = 20;

	/**
	 * @param pc
	 * @param s
	 * @param date
	 * @param date1
	 * @return
	 * @throws ExpressionException
	 */
	public synchronized static double call(PageContext pc , String datePart, DateTime left, DateTime right) throws ExpressionException	{
		long msLeft = left.getTime();
		long msRight = right.getTime();
		TimeZone tz =ThreadLocalPageContext.getTimeZone(pc);
		// Date Part
		datePart=datePart.toLowerCase().trim();
		int dp;
		if("s".equals(datePart))		return diffSeconds(msLeft, msRight);
		else if("n".equals(datePart))	return diffSeconds(msLeft, msRight)/60L;
		else if("h".equals(datePart))	return diffSeconds(msLeft, msRight)/3600L;
		else if("d".equals(datePart))	dp=DATEPART_D;
		else if("y".equals(datePart))	dp=DATEPART_Y;
		else if("yyyy".equals(datePart))dp=DATEPART_YYYY;
		else if("m".equals(datePart))	dp=DATEPART_M;
		else if("w".equals(datePart))	dp=DATEPART_W;
		else if("ww".equals(datePart))	dp=DATEPART_WW;
		else if("q".equals(datePart))	dp=DATEPART_Q;
		else throw new FunctionException(pc,"dateDiff",3,"datePart","invalid value ["+datePart+"], valid values has to be [q,s,n,h,d,m,y,yyyy,w,ww]");

		// dates
		Calendar _cLeft = JREDateTimeUtil.getThreadCalendar(tz);
		_cLeft.setTimeInMillis(msLeft);
		
		Calendar _cRight = JREDateTimeUtil.newInstance(tz,Locale.US);
		_cRight.setTimeInMillis(msRight);
			
			
			if(msLeft>msRight) 
				return -_call(pc,dp, _cRight, msRight, _cLeft, msLeft);
			
			return _call(pc,dp, _cLeft, msLeft, _cRight, msRight);
		//}
	}
	
	public static long diffSeconds(long msLeft, long msRight) {
		if(msLeft>msRight)
			return -(long)((msLeft-msRight)/1000D);
		return (long)((msRight-msLeft)/1000D);
	}

	private static long _call(PageContext pc , int datepart, Calendar cLeft, long msLeft, Calendar cRight, long msRight) throws ExpressionException {
		long dDiff = cRight.get(Calendar.DATE)-cLeft.get(Calendar.DATE);
		long hDiff = cRight.get(Calendar.HOUR_OF_DAY)-cLeft.get(Calendar.HOUR_OF_DAY);
		long nDiff = cRight.get(Calendar.MINUTE)-cLeft.get(Calendar.MINUTE);
		long sDiff = cRight.get(Calendar.SECOND)-cLeft.get(Calendar.SECOND);
		

		if(DATEPART_D==datepart || DATEPART_W==datepart)	{
			int tmp=0;
			if(hDiff<0) 	tmp=-1;
			else if(hDiff>0);
			else if(nDiff<0) 	tmp=-1;
			else if(nDiff>0);
			else if(sDiff<0) 	tmp=-1;
			else if(sDiff>0);
			long rst = dayDiff(cLeft, cRight)+tmp;
			if(DATEPART_W==datepart)rst/=7;
			return rst;
		}
		
		long yDiff = cRight.get(Calendar.YEAR)-cLeft.get(Calendar.YEAR);
		long mDiff = cRight.get(Calendar.MONTH)-cLeft.get(Calendar.MONTH);
		if(DATEPART_YYYY==datepart)	{
			int tmp=0;
			if(mDiff<0)			tmp=-1;
			else if(mDiff>0);
			else if(dDiff<0) 	tmp=-1;
			else if(dDiff>0);
			else if(hDiff<0) 	tmp=-1;
			else if(hDiff>0);
			else if(nDiff<0) 	tmp=-1;
			else if(nDiff>0);
			else if(sDiff<0) 	tmp=-1;
			else if(sDiff>0);
			return yDiff+tmp;
		}
		if(DATEPART_M==datepart || DATEPART_Q==datepart)	{
			int tmp=0;
			if(dDiff<0 && isEndOfMonth(cRight)) dDiff=0;
			if(dDiff<0) 		tmp=-1;
			else if(dDiff>0);
			else if(hDiff<0) 	tmp=-1;
			else if(hDiff>0);
			else if(nDiff<0) 	tmp=-1;
			else if(nDiff>0);
			else if(sDiff<0) 	tmp=-1;
			else if(sDiff>0);
			long rst = mDiff+(yDiff*12)+tmp;
			if(DATEPART_Q==datepart)rst/=3;
			return rst;
		}
		if(DATEPART_D==datepart)	{
			return dDiff;
		}
		throw new FunctionException(pc,"dateDiff",3,"datePart","invalid value, valid values has to be [q,s,n,h,d,m,y,yyyy,w,ww]");
		
	}

	private static boolean isEndOfMonth(Calendar cal) {
		return cal.get(Calendar.DATE) == cal.getActualMaximum(Calendar.DATE);
	}

	private static long dayDiff(Calendar l, Calendar r) {
		int lYear = l.get(Calendar.YEAR);
		int rYear = r.get(Calendar.YEAR);
		int lDayOfYear=l.get(Calendar.DAY_OF_YEAR);
		int rDayOfYear=r.get(Calendar.DAY_OF_YEAR);

		// same year
		if(lYear==rYear){
			return rDayOfYear-lDayOfYear;
		}
		
		long diff=rDayOfYear;
		diff-=lDayOfYear;
		for(int year=lYear;year<rYear;year++){
			diff+=Decision.isLeapYear(year)?366L:365L;
		}
		return diff;
	}

	public Object invokeo(PageContext pc, Object[] args) throws PageException {
		// public synchronized static double call(PageContext pc , String datePart, DateTime left, DateTime right) throws ExpressionException	{
		return null;
	}
	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==3)
			return call(pc, Caster.toString(args[0]), Caster.toDateTime(args[1],null), Caster.toDateTime(args[2],null));
		throw new FunctionException(pc,"DateDiff",3,3,args.length);
	}
}