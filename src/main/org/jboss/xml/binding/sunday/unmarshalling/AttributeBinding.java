/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xml.binding.sunday.unmarshalling;

import org.jboss.xml.binding.metadata.PropertyMetaData;
import org.jboss.xml.binding.metadata.ValueMetaData;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class AttributeBinding
{
   private final SchemaBinding schema;
   private final TypeBinding type;
   private AttributeHandler handler;
   private PropertyMetaData propertyMetaData;
   private ValueMetaData valueMetaData;
   private boolean mapEntryKey;
   private boolean mapEntryValue;

   public AttributeBinding(SchemaBinding schema, TypeBinding type, AttributeHandler handler)
   {
      this.schema = schema;
      this.type = type;
      this.handler = handler;
   }

   public TypeBinding getType()
   {
      return type;
   }

   public AttributeHandler getHandler()
   {
      return handler;
   }

   public void setHandler(AttributeHandler handler)
   {
      this.handler = handler;
   }

   public PropertyMetaData getPropertyMetaData()
   {
      return propertyMetaData;
   }

   public void setPropertyMetaData(PropertyMetaData propertyMetaData)
   {
      this.propertyMetaData = propertyMetaData;
   }

   public ValueMetaData getValueMetaData()
   {
      return valueMetaData;
   }

   public void setValueMetaData(ValueMetaData valueMetaData)
   {
      this.valueMetaData = valueMetaData;
   }

   public void setMapEntryKey(boolean mapEntryKey)
   {
      this.mapEntryKey = mapEntryKey;
   }

   public boolean isMapEntryKey()
   {
      return mapEntryKey;
   }

   public boolean isMapEntryValue()
   {
      return mapEntryValue;
   }

   public void setMapEntryValue(boolean mapEntryValue)
   {
      this.mapEntryValue = mapEntryValue;
   }

   public SchemaBinding getSchema()
   {
      return schema;
   }
}
