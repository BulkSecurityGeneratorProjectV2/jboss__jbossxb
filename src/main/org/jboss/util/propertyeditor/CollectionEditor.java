/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.propertyeditor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;

import java.beans.PropertyEditorSupport;

/**
 * A property editor for {@link java.util.Collection}.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CollectionEditor
   extends PropertyEditorSupport
{
   protected Collection createCollection()
   {
      return new LinkedList();
   }
   
   public void setAsText(final String text)
   {
      Collection bag = createCollection();
      StringTokenizer stok = new StringTokenizer(text, ",");

      // need to handle possible "[" and "]"
      
      while (stok.hasMoreTokens()) {
         bag.add(stok.nextToken().trim());
      }
      
      setValue(bag);
   }
}