/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.util.property;

import org.jboss.util.FieldInstance;
import org.jboss.util.NullArgumentException;
import org.jboss.util.Classes;
import org.jboss.util.Objects;
import org.jboss.util.CoercionException;
import org.jboss.util.ThrowableHandler;

/**
 * Binds property values to class fields.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class FieldBoundPropertyListener
   extends BoundPropertyAdapter
{
   /** Property name which we are bound to */
   protected final String propertyName;

   /** Field instance */
   protected final FieldInstance fieldInstance;

   /**
    * Constructs a <tt>FieldBoundPropertyListener</tt>.
    *
    * @param instance         Instance object.
    * @param fieldName        Field name.
    * @param propertyName     Property to bind to.
    *
    * @throws NullArgumentException    Property name is <tt>null</tt>.
    */
   public FieldBoundPropertyListener(final Object instance,
                                     final String fieldName,
                                     final String propertyName)
   {
      if (propertyName == null)
         throw new NullArgumentException("propertyName");
      // FieldInstance checks instance & fieldName

      this.propertyName = propertyName;

      try {
         // construct field instance
         fieldInstance = new FieldInstance(instance, fieldName);
         try {
            fieldInstance.getField().setAccessible(true);
         }
         catch (SecurityException e) {
            ThrowableHandler.add(e);
         }

         // force the given class to load, so that any CoersionHelpers
         // that are nested in it are loaded properly
         Classes.forceLoad(fieldInstance.getField().getType());
      }
      catch (NoSuchFieldException e) {
         throw new PropertyException(e);
      }
   }

   /**
    * Constructs a <tt>FieldBoundPropertyListener</tt>.
    *
    * <p>Field name is used for property name.
    *
    * @param instance         Instance object.
    * @param fieldName        Field name.
    */
   public FieldBoundPropertyListener(final Object instance,
                                     final String fieldName)
   {
      this(instance, fieldName, fieldName);
   }

   /**
    * Get the property name which this listener is bound to.
    *
    * @return     Property name.
    */
   public final String getPropertyName() {
      return propertyName;
   }

   /**
    * Filter the property value prior to coercing and binding to field.
    *
    * <p>Allows instance to filter values prior to object coercion and
    *    field binding.
    *
    * @param value   Property value.
    */
   public String filterValue(String value) {
      return value;
   }

   /**
    * Coerce and set specified value to field.
    *
    * @param value   Field value.
    *
    * @throws PropertyException     Failed to set field value.
    */
   protected void setFieldValue(String value) {
      try {
         // filter property value
         value = filterValue(value);

         // coerce value to field type
         Class type = fieldInstance.getField().getType();
         Object coerced = Objects.coerce(value, type);

         // bind value to field
         fieldInstance.set(coerced);
      }
      catch (IllegalAccessException e) {
         throw new PropertyException(e);
      }
      catch (CoercionException e) {
         throw new PropertyException(e);
      }
   }

   /**
    * Notifies that a property has been added.
    *
    * @param event   Property event.
    */
   public void propertyAdded(final PropertyEvent event) {
      setFieldValue(event.getPropertyValue());
   }

   /**
    * Notifies that a property has changed
    *
    * @param event   Property event
    */
   public void propertyChanged(final PropertyEvent event) {
      setFieldValue(event.getPropertyValue());
   }

   /**
    * Notifies that this listener was bound to a property.
    *
    * @param map     PropertyMap which contains property bound to.
    */
   public void propertyBound(final PropertyMap map) {
      // only set the field if the map contains the property already
      if (map.containsProperty(propertyName)) {
         setFieldValue(map.getProperty(propertyName));
      }
   }
}
