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
/*
*

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package lucee.runtime.img.vecmath;

/**
 * Vector math package, converted to look similar to javax.vecmath.
 */
public class Vector4f extends Tuple4f {

	public Vector4f() {
		this( 0, 0, 0, 0 );
	}
	
	public Vector4f( float[] x ) {
		this.x = x[0];
		this.y = x[1];
		this.z = x[2];
		this.w = x[2];
	}

	public Vector4f( float x, float y, float z, float w ) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vector4f( Vector4f t ) {
		x = t.x;
		y = t.y;
		z = t.z;
		w = t.w;
	}

	public Vector4f( Tuple4f t ) {
		x = t.x;
		y = t.y;
		z = t.z;
		w = t.w;
	}

	public float dot( Vector4f v ) {
		return v.x * x + v.y * y + v.z * z + v.w * w;
	}

	public float length() {
		return (float)Math.sqrt( x*x+y*y+z*z+w*w );
	}

	public void normalize() {
		float d = 1.0f/( x*x+y*y+z*z+w*w );
		x *= d;
		y *= d;
		z *= d;
		w *= d;
	}

}