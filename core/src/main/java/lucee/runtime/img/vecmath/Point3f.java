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
public class Point3f extends Tuple3f {

	public Point3f() {
		this( 0, 0, 0 );
	}
	
	public Point3f( float[] x ) {
		this.x = x[0];
		this.y = x[1];
		this.z = x[2];
	}

	public Point3f( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point3f( Point3f t ) {
		this.x = t.x;
		this.y = t.y;
		this.z = t.z;
	}

	public Point3f( Tuple3f t ) {
		this.x = t.x;
		this.y = t.y;
		this.z = t.z;
	}

	public float distanceL1( Point3f p ) {
		return Math.abs(x-p.x) + Math.abs(y-p.y) + Math.abs(z-p.z);
	}

	public float distanceSquared( Point3f p ) {
		float dx = x-p.x;
		float dy = y-p.y;
		float dz = z-p.z;
		return dx*dx+dy*dy+dz*dz;
	}

	public float distance( Point3f p ) {
		float dx = x-p.x;
		float dy = y-p.y;
		float dz = z-p.z;
		return (float)Math.sqrt( dx*dx+dy*dy+dz*dz );
	}

}