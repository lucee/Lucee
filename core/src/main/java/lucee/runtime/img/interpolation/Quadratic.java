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

public class Quadratic implements Interpolation
{
    @Override
	public double f(double x) {
	if (x < 0.0)
	    x = -x;
	if (x < 0.5)
	    return 0.75 - x * x;
	if (x < 1.5) {
	    x -= 1.5;
	    return 0.5 * x * x;
	}
	return 0.0;
    }
    
    @Override
	public double getSupport() {
	return 1.5;
    }
}