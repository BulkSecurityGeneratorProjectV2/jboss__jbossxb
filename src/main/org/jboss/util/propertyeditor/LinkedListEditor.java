/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.propertyeditor;

import java.util.LinkedList;
import java.util.List;

/**
 * A property editor for {@link java.util.LinkedList}.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class LinkedListEditor
   extends ListEditor
{
   protected List createList()
   {
      return new LinkedList();
   }
}
