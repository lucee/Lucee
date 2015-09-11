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
package lucee.runtime.chart;

import java.util.Locale;

import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.international.LSCurrencyFormat;
import lucee.runtime.functions.international.LSDateFormat;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;

public class LabelFormatUtil {


	public static final int LABEL_FORMAT_NUMBER = 0;
	public static final int LABEL_FORMAT_CURRENCY = 1;
	public static final int LABEL_FORMAT_PERCENT = 2;
	public static final int LABEL_FORMAT_DATE = 3;

	public static String formatDate(PageContext pc,double value) {
		DateTime d = Caster.toDate(Caster.toDouble(value),true,null,null);
		
		try {
			return LSDateFormat.call(pc, d);
		} catch (PageException e) {
		}
		return Caster.toString(d,null);
	}

	public static String formatNumber(double value) {
		return Caster.toString(value);
	}

	public static String formatPercent(double value) {
		return Caster.toIntValue(value*100)+" %";
	}

	public static String formatCurrency(PageContext pc,double value) {
		//PageContext pc = Thread LocalPageContext.get();
		Locale locale=pc==null?Locale.US:pc.getLocale();
		return LSCurrencyFormat.local(locale, value);
	}

	public static String format(int labelFormat, double value) {
		
		switch(labelFormat) {
		case LABEL_FORMAT_CURRENCY:	return formatCurrency(ThreadLocalPageContext.get(),value);
		case LABEL_FORMAT_DATE:		return formatDate(ThreadLocalPageContext.get(),value);
		case LABEL_FORMAT_NUMBER:	return formatNumber(value);
		case LABEL_FORMAT_PERCENT:	return formatPercent(value);
		}
		return Caster.toString(value);
	}
}