/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xml.binding.sunday.unmarshalling;

import org.xml.sax.Attributes;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public interface ElementHandler
{
   AttributeType addAttribute(QName name);

   void addAttribute(QName name, AttributeType binding);

   ElementHandler pushAttributeHandler(QName name, AttributeHandler handler);

   void setTextContentBinding(TextContent binding);

   ElementHandler pushTextContentHandler(TextContentHandler handler);

   ElementType addElement(QName name);

   void addElement(QName name, ElementType binding);

   ElementType getElement(QName name);

   Object start(Object parent, QName name, Attributes attrs);

   void end(Object parent, Object child, QName name, String dataContent);
}
