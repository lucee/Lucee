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

public class Lanczos implements Interpolation
{
    @Override
	public double f(double x) {
	if (x < -3.0)
	    return 0.0;
	if (x < 0.0)
	    return sinc(-x) * sinc(-x / 3.0);
	if (x < 3.0)
	    return sinc(x) * sinc(x / 3.0);
	return 0.0;
    }
    
    public double sinc(double x) {
	x *= 3.141592653589793;
	if (x != 0.0)
	    return Math.sin(x) / x;
	return 1.0;
    }
    
    @Override
	public double getSupport() {
	return 3.0;
    }
}