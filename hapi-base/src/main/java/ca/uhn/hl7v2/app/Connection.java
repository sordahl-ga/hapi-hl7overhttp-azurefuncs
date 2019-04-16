/*
 The contents of this file are subject to the Mozilla Public License Version 1.1
 (the "License"); you may not use this file except in compliance with the License.
 You may obtain a copy of the License at http://www.mozilla.org/MPL/
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 specific language governing rights and limitations under the License.

 The Original Code is "Connection.java".  Description:
 "Interface for a Connection"

 The Initial Developer of the Original Code is University Health Network. Copyright (C)
 2013.  All Rights Reserved.

 Contributor(s): ______________________________________.

 Alternatively, the contents of this file may be used under the terms of the
 GNU General Public License (the "GPL"), in which case the provisions of the GPL are
 applicable instead of those above.  If you wish to allow use of your version of this
 file only under the terms of the GPL and not to allow others to use your version
 of this file under the MPL, indicate your decision by deleting  the provisions above
 and replace  them with the notice and other provisions required by the GPL License.
 If you do not delete the provisions above, a recipient may use your version of
 this file under either the MPL or the GPL.
 */

package ca.uhn.hl7v2.app;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

/**
 * Interface for a connection
 */
public interface Connection {

    /**
     * Activate this connection
     */
    void activate();

    /**
     * Returns an Initiator instance
     * @return
     */
    Initiator getInitiator();


    /**
     * Closes the connection
     */
    void close();

    /**
     * Returns true if this connection is open
     *
     * @return true if this connection is open
     */
    boolean isOpen();

    /**
     * Returns the address on the remote end of this connection. Note that in the case
     * of a {@link LazyConnection} this method may return <code>null</code>.
     * 
     * @see #getRemotePort()
     */
    InetAddress getRemoteAddress();

    ExecutorService getExecutorService();

    /**
     * Returns the port on the remote end of this connection. Note that in the case
     * of a {@link LazyConnection} this method may return <code>null</code>.
     * 
     * @see #getRemoteAddress()
     */
	Integer getRemotePort();
}
