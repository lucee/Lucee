/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.net.ftp;

import java.io.IOException;

import lucee.runtime.exp.PageException;

import org.apache.commons.net.ftp.FTPClient;

/**
 * FTP Pool
 */
public interface FTPPool {

    /**
     * returns a FTPClient from the pool, if no matching exist, create a new one
     * @param conn
     * @return Matching FTP Client
     * @throws IOException
     * @throws PageException
     */
    public abstract FTPClient get(FTPConnection conn) throws IOException, PageException;

    /**
     * removes a FTPConnection from pool andreturn it (disconnected)
     * @param conn 
     * @return disconnetd Client
     */
    public abstract FTPClient remove(FTPConnection conn);

    /**
     * removes a FTPConnection from pool andreturn it (disconnected)
     * @param name Name of the connection to remove
     * @return disconnetd Client
     */
    public abstract FTPClient remove(String name);

    /**
     * clears all connection from pool
     */
    public abstract void clear();

}