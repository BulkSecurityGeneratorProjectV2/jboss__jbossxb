/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xb.binding.metadata.unmarshalling;

import javax.xml.namespace.QName;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public interface BasicElementBinding
   extends XmlValueContainer
{
   DocumentBinding getDocument();

   ElementBinding getElement(QName elementName);

   AttributeBinding getAttribute(QName attributeName);
}
