/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xb.binding.sunday.unmarshalling;

import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import org.xml.sax.Attributes;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class DefaultElementInterceptor
   implements ElementInterceptor
{
   public static final ElementInterceptor INSTANCE = new DefaultElementInterceptor();

   public Object startElement(Object parent, QName qName, TypeBinding type)
   {
      return parent;
   }

   public void attributes(Object o, QName elementName, TypeBinding type, Attributes attrs, NamespaceContext nsCtx)
   {
   }

   public void characters(Object o, QName qName, TypeBinding type, NamespaceContext nsCtx, String text)
   {
   }

   public Object endElement(Object o, QName qName, TypeBinding type)
   {
      return o;
   }

   public void add(Object parent, Object child, QName qName)
   {
   }
}
