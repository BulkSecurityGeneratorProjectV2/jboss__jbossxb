/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xb.binding.group;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.sunday.unmarshalling.AttributeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class ValueListInitializer
{
   private int initializedState;
   private Map attrIndex = Collections.EMPTY_MAP;
   private Map elemIndex = Collections.EMPTY_MAP;
   private List requiredBindings = Collections.EMPTY_LIST;

   public void addRequiredAttribute(QName qName, AttributeBinding binding)
   {
      Integer index = new Integer(requiredBindings.size());
      switch(attrIndex.size())
      {
         case 0:
            attrIndex = Collections.singletonMap(qName, index);
            break;
         case 1:
            attrIndex = new HashMap(attrIndex);
         default:
            attrIndex.put(qName, index);
      }
      addBinding(binding);
      initializedState += Math.abs(qName.hashCode());
   }

   public void addRequiredElement(QName qName, ElementBinding binding)
   {
      Integer index = new Integer(requiredBindings.size());
      switch(elemIndex.size())
      {
         case 0:
            elemIndex = Collections.singletonMap(qName, index);
            break;
         case 1:
            elemIndex = new HashMap(elemIndex);
         default:
            elemIndex.put(qName, index);
      }
      addBinding(binding);
      initializedState += Math.abs(qName.hashCode());
   }

   public ValueList newValueList(ValueListHandler handler, Class targetClass)
   {
      return new ValueList(this, handler, targetClass);
   }

   public void addAttributeValue(QName qName, AttributeBinding binding, ValueList valueList, Object value)
   {
      Integer index = (Integer)attrIndex.get(qName);
      if(index == null)
      {
         valueList.setNonRequiredValue(qName, binding, value);
      }
      else
      {
         if(isInitialized(valueList))
         {
            throw new JBossXBRuntimeException("The value list has already been initialized!");
         }
         valueList.setRequiredValue(index.intValue(), qName.hashCode(), value);
      }
   }

   public void addElementValue(QName qName, ElementBinding binding, ValueList valueList, Object value)
   {
      Integer index = (Integer)elemIndex.get(qName);
      if(index == null)
      {
         valueList.setNonRequiredValue(qName, binding, value);
      }
      else
      {
         if(isInitialized(valueList))
         {
            throw new JBossXBRuntimeException("The value list has already been initialized!");
         }
         valueList.setRequiredValue(index.intValue(), qName.hashCode(), value);
      }
   }

   public boolean isInitialized(ValueList valueList)
   {
      return requiredBindings.size() == 0 || initializedState == valueList.getState();
   }

   public Object getAttributeValue(QName qName, ValueList valueList)
   {
      Object value;
      Integer index = (Integer)attrIndex.get(qName);
      if(index == null)
      {
         value = valueList.getNonRequiredValue(qName);
      }
      else
      {
         value = valueList.getRequiredValue(index.intValue());
      }
      return value;
   }

   public Object getElementValue(QName qName, ValueList valueList)
   {
      Object value;
      Integer index = (Integer)elemIndex.get(qName);
      if(index == null)
      {
         value = valueList.getNonRequiredValue(qName);
      }
      else
      {
         value = valueList.getRequiredValue(index.intValue());
      }
      return value;
   }

   public List getRequiredBindings()
   {
      return requiredBindings;
   }

   // Private

   private void addBinding(Object binding)
   {
      if(requiredBindings == Collections.EMPTY_LIST)
      {
         requiredBindings = new ArrayList();
      }
      requiredBindings.add(binding);
   }
}
