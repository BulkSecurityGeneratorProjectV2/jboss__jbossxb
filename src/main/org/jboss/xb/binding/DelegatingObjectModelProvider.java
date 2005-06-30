/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xb.binding;

/**
 * todo come up with a nicer class name
 * 
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class DelegatingObjectModelProvider
   implements GenericObjectModelProvider
{
   private final ObjectModelProvider provider;

   public DelegatingObjectModelProvider(ObjectModelProvider provider)
   {
      this.provider = provider;
   }

   public Object getChildren(Object o, MarshallingContext ctx, String namespaceURI, String localName)
   {
      return AbstractMarshaller.provideChildren(provider, o, namespaceURI, localName);
   }

   public Object getElementValue(Object o, MarshallingContext ctx, String namespaceURI, String localName)
   {
      return AbstractMarshaller.provideValue(provider, o, namespaceURI, localName);
   }

   public Object getAttributeValue(Object o, MarshallingContext ctx, String namespaceURI, String localName)
   {
      return AbstractMarshaller.provideAttributeValue(provider, o, namespaceURI, localName);
   }

   public Object getRoot(Object o, MarshallingContext ctx, String namespaceURI, String localName)
   {
      return provider.getRoot(o, null, namespaceURI, localName);
   }
}