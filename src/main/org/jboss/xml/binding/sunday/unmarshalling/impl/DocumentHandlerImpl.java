/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xml.binding.sunday.unmarshalling.impl;

import org.jboss.xml.binding.sunday.unmarshalling.DocumentHandler;
import org.jboss.xml.binding.sunday.unmarshalling.ElementBinding;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class DocumentHandlerImpl
   implements DocumentHandler
{
   private Map topElements = new HashMap();

   public ElementBinding addElement(QName name)
   {
      ElementBinding binding = new ElementBindingImpl();
      addElement(name, binding);
      return binding;
   }

   public void addElement(QName name, ElementBinding binding)
   {
      topElements.put(name, binding);
   }

   public ElementBinding getElement(QName name)
   {
      return (ElementBinding)topElements.get(name);
   }
/*
   public void endElement(String namespaceURI, String localName, String qName) throws SAXException
   {
      ElementStack.StackItem stackItem = elementStack.pop();
      if(stackItem != ElementStack.NULL_ITEM)
      {
         String dataContent = null;
         if(textContent.length() > 0)
         {
            dataContent = textContent.toString();
            textContent.delete(0, textContent.length());
         }

         Object child = stackItem.binding.end(stackItem.parent,
            stackItem.name,
            objectStack,
            stackItem.startIndex,
            stackItem.endIndex,
            dataContent
         );

         if(elementStack.isEmpty())
         {
            root = child;
         }
      }
   }

   public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
   {
      QName startName = localName.length() == 0 ? new QName(qName) : new QName(namespaceURI, localName);

      ElementStack.StackItem stackItem = ElementStack.NULL_ITEM;
      if(elementStack.isEmpty())
      {
         ElementBinding element = (ElementBinding)topElements.get(startName);
         if(element != null)
         {
            stackItem = new ElementStack.StackItem(startName, element, null, objectStack.size());
         }
      }
      else
      {
         ElementStack.StackItem parentItem = elementStack.peek();
         if(parentItem != ElementStack.NULL_ITEM)
         {
            ElementBinding element = null;
            List handlers = parentItem.binding.getElementHandlers();
            for(int i = 0; i < handlers.size(); ++i)
            {
               ElementHandler handler = (ElementHandler)handlers.get(i);
               element = handler.getElement(startName);
               if(element != null)
               {
                  stackItem =
                     new ElementStack.StackItem(startName, element,
                        objectStack.peek(parentItem.startIndex + i),
                        objectStack.size()
                     );
                  break;
               }
            }

            if(log.isTraceEnabled() && element == null)
            {
               log.warn("element not bound: " + startName);
            }
         }
      }

      elementStack.push(stackItem);
      if(stackItem != ElementStack.NULL_ITEM)
      {
         stackItem.endIndex = stackItem.binding.start(stackItem.parent,
            stackItem.name,
            atts,
            objectStack,
            stackItem.startIndex
         );
      }
   }
  */
}