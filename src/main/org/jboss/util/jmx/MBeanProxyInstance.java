/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * An interface which exposes the attributes of a {@link MBeanProxy}
 * instance.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision$
 */
public interface MBeanProxyInstance
{
   /**
    * Return the ObjectName for this proxy.
    *
    * @return   The ObjectName for this proxy.
    */
   ObjectName getMBeanProxyObjectName();

   /**
    * Return the MBeanServer for this proxy.
    *
    * @return   The ObjectName for this proxy.
    */
   MBeanServer getMBeanProxyMBeanServer();
}
