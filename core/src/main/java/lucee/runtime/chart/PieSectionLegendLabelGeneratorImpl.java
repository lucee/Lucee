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

import java.awt.Font;
import java.awt.FontMetrics;
import java.text.AttributedString;

import lucee.commons.lang.StringList;
import lucee.commons.lang.font.FontUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.data.general.PieDataset;

public class PieSectionLegendLabelGeneratorImpl implements
		PieSectionLabelGenerator {


	private FontMetrics metrics;
	private int with;

	public PieSectionLegendLabelGeneratorImpl(Font font,int with) {
		this.metrics=FontUtil.getFontMetrics(font);
		this.with=with-20;
	}

	@Override
	public AttributedString generateAttributedSectionLabel(PieDataset dataset,
			Comparable key) {
		return null;
	}

	@Override
	public String generateSectionLabel(PieDataset pd, Comparable c) {
		String value=Caster.toString(pd.getKey(pd.getIndex(c)),"");
		
		StringList list = ListUtil.toList(value, '\n');
		StringBuilder sb=new StringBuilder();
		String line;
		int lineLen;
		while(list.hasNext()) {
			line=list.next();
			lineLen=metrics.stringWidth(line);
			if(lineLen>with) {
				reorganize(sb,list,new StringBuilder(line));
				break;
			}
			if(sb.length()>0)sb.append('\n');
			sb.append(line);
		}
		
		
		
		//int strLen = metrics.stringWidth(value);
		return sb.toString();//metrics.stringWidth(value)+"-"+with+":"+value;
		//return "StringUtil.reverse()";
	}

	private void reorganize(StringBuilder sb, StringList list, StringBuilder rest) {
		// fill rest
		String item;
		while(list.hasNext()) {
			item=list.next();
			rest.append(list.delimiter());
			rest.append(item);
		}
		
		StringList words = ListUtil.toWordList(rest.toString());
		StringBuffer line=new StringBuffer();
		
		while(words.hasNext()) {
			item=words.next();
			
			if(line.length()>0 && metrics.stringWidth(item.concat(" ").concat(line.toString()))>with) {
				if(sb.length()>0) sb.append('\n');
				sb.append(line);
				//print.out("line:"+line);
				line=new StringBuffer(item);
			}
			else {
				//item=words.next();
				if(line.length()>0)line.append(words.delimiter()==0?' ':words.delimiter());
				line.append(item);
			}
		}
		if(line.length()>0){
			if(sb.length()>0) sb.append('\n');
			sb.append(line);
		}
	}
}