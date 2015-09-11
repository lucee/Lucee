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
package lucee.runtime.img.interpolation;

public class Mitchell implements Interpolation
{
    @Override
	public double f(double x) {
	double b = 0.3333333333333333;
	double c = 0.3333333333333333;
	if (x < 0.0)
	    x = -x;
	if (x < 1.0) {
	    x = ((12.0 - 9.0 * b - 6.0 * c) * (x * x * x)
		 + (-18.0 + 12.0 * b + 6.0 * c) * x * x + (6.0 - 2.0 * b));
	    return x / 6.0;
	}
	if (x < 2.0) {
	    x = ((-1.0 * b - 6.0 * c) * (x * x * x)
		 + (6.0 * b + 30.0 * c) * x * x + (-12.0 * b - 48.0 * c) * x
		 + (8.0 * b + 24.0 * c));
	    return x / 6.0;
	}
	return 0.0;
    }
    
    @Override
	public double getSupport() {
	return 2.0;
    }
}