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

import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.data.general.PieDataset;

public class PieToolTipGeneratorImpl implements PieToolTipGenerator {

	
	
	private int labelFormat;

	/**
	 * Constructor of the class
	 * @param labelFormat
	 */
	public PieToolTipGeneratorImpl(int labelFormat) {
		this.labelFormat=labelFormat;
	}

	@Override
	public String generateToolTip(PieDataset dataset, Comparable key) {
		
		String result = null;    
        if (dataset != null) {
            result=LabelFormatUtil.format(labelFormat,dataset.getValue(key).doubleValue());
        }
        return result;
		
		// TODO Auto-generated method stub
		//return toolTipGenerator.generateToolTip(dataset, key);
	}

}