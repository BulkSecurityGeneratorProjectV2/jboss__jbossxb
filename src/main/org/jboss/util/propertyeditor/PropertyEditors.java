/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.propertyeditor;

import java.beans.IntrospectionException;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Properties;
import java.util.Iterator;
import java.util.HashMap;
import java.lang.reflect.Method;

import org.jboss.util.Classes;
import org.jboss.logging.Logger;

/**
 * A collection of PropertyEditor utilities.  Provides the same interface
 * as PropertyManagerEditor plus more...
 *
 * <p>Installs the default PropertyEditors.
 *
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @version <tt>$Revision$</tt>
 */
public class PropertyEditors
{
   private static Logger log = Logger.getLogger(PropertyEditors.class);

   /** Augment the PropertyEditorManager search path to incorporate the JBoss
    specific editors by appending the org.jboss.util.propertyeditor package
    to the PropertyEditorManager editor search path.
    */
   static
   {
      String[] currentPath = PropertyEditorManager.getEditorSearchPath();
      int length = currentPath != null ? currentPath.length : 0;
      String[] newPath = new String[length+2];
      System.arraycopy(currentPath, 0, newPath, 2, length);
      // Put the JBoss editor path first
      // The default editors are not very flexible
      newPath[0] = "org.jboss.util.propertyeditor";
      newPath[1] = "org.jboss.mx.util.propertyeditor";
      PropertyEditorManager.setEditorSearchPath(newPath);

      /* Register the editor types that will not be found using the standard
      class name to editor name algorithm. For example, the type String[] has
      a name '[Ljava.lang.String;' which does not map to a XXXEditor name.
      */
      Class strArrayType = String[].class;
      PropertyEditorManager.registerEditor(strArrayType, StringArrayEditor.class);
      Class clsArrayType = Class[].class;
      PropertyEditorManager.registerEditor(clsArrayType, ClassArrayEditor.class);
      Class intArrayType = int[].class;
      PropertyEditorManager.registerEditor(intArrayType, IntArrayEditor.class);
   }

   /**
    * Locate a value editor for a given target type.
    *
    * @param type   The class of the object to be edited.
    * @return       An editor for the given type or null if none was found.
    */
   public static PropertyEditor findEditor(final Class type)
   {
      return PropertyEditorManager.findEditor(type);
   }

   /**
    * Locate a value editor for a given target type.
    *
    * @param typeName    The class name of the object to be edited.
    * @return            An editor for the given type or null if none was found.
    */
   public static PropertyEditor findEditor(final String typeName)
      throws ClassNotFoundException
   {
      // see if it is a primitive type first
      Class type = Classes.getPrimitiveTypeForName(typeName);
      if (type == null)
      {
         // nope try look up
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         type = loader.loadClass(typeName);
      }

      return PropertyEditorManager.findEditor(type);
   }

   /**
    * Get a value editor for a given target type.
    *
    * @param type    The class of the object to be edited.
    * @return        An editor for the given type.
    *
    * @throws RuntimeException   No editor was found.
    */
   public static PropertyEditor getEditor(final Class type)
   {
      PropertyEditor editor = findEditor(type);
      if (editor == null)
      {
         throw new RuntimeException("No property editor for type: " + type);
      }

      return editor;
   }

   /**
    * Get a value editor for a given target type.
    *
    * @param typeName    The class name of the object to be edited.
    * @return            An editor for the given type.
    *
    * @throws RuntimeException   No editor was found.
    */
   public static PropertyEditor getEditor(final String typeName)
      throws ClassNotFoundException
   {
      PropertyEditor editor = findEditor(typeName);
      if (editor == null)
      {
         throw new RuntimeException("No property editor for type: " + typeName);
      }

      return editor;
   }

   /**
    * Register an editor class to be used to editor values of a given target class.
    *
    * @param type         The class of the objetcs to be edited.
    * @param editorType   The class of the editor.
    */
   public static void registerEditor(final Class type, final Class editorType)
   {
      PropertyEditorManager.registerEditor(type, editorType);
   }

   /**
    * Register an editor class to be used to editor values of a given target class.
    *
    * @param typeName         The classname of the objetcs to be edited.
    * @param editorTypeName   The class of the editor.
    */
   public static void registerEditor(final String typeName,
                                     final String editorTypeName)
      throws ClassNotFoundException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class type = loader.loadClass(typeName);
      Class editorType = loader.loadClass(editorTypeName);

      PropertyEditorManager.registerEditor(type, editorType);
   }

   /** Convert a string value into the true value for typeName using the
    * PropertyEditor associated with typeName.
    *
    * @param text the string represention of the value. This is passed to
    * the PropertyEditor.setAsText method.
    * @param typeName the fully qualified class name of the true value type
    * @return the PropertyEditor.getValue() result
    * @exception ClassNotFoundException thrown if the typeName class cannot
    *    be found
    * @exception IntrospectionException thrown if a PropertyEditor for typeName
    *    cannot be found
    */
   public static Object convertValue(String text, String typeName)
         throws ClassNotFoundException, IntrospectionException
   {
      // see if it is a primitive type first
      Class typeClass = Classes.getPrimitiveTypeForName(typeName);
      if (typeClass == null)
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         typeClass = loader.loadClass(typeName);
      }

      PropertyEditor editor = PropertyEditorManager.findEditor(typeClass);
      if (editor == null)
      {
         throw new IntrospectionException
               ("No property editor for type=" + typeClass);
      }

      editor.setAsText(text);
      return editor.getValue();
   }

   /**
    * This method takes the properties found in the given beanProps
    * to the bean using the property editor registered for the property.
    * Any property in beanProps that does not have an associated java bean
    * property will result in an IntrospectionException. The string property
    * values are converted to the true java bean property type using the
    * java bean PropertyEditor framework. If a property in beanProps does not
    * have a PropertyEditor registered it will be ignored.
    *
    * @param bean - the java bean instance to apply the properties to
    * @param beanProps - map of java bean property name to property value.
    * @throws IntrospectionException thrown on introspection of bean and if
    *    a property in beanProps does not map to a property of bean.
    */
   public static void mapJavaBeanProperties(Object bean, Properties beanProps)
      throws IntrospectionException
   {
      mapJavaBeanProperties(bean, beanProps, true);
   }

   /**
    * This method takes the properties found in the given beanProps
    * to the bean using the property editor registered for the property.
    * Any property in beanProps that does not have an associated java bean
    * property will result in an IntrospectionException. The string property
    * values are converted to the true java bean property type using the
    * java bean PropertyEditor framework. If a property in beanProps does not
    * have a PropertyEditor registered it will be ignored.
    *
    * @param bean - the java bean instance to apply the properties to
    * @param beanProps - map of java bean property name to property value.
    * @param isStrict - indicates if should throw exception if bean property can not
    * be matched.  True for yes, false for no.
    * @throws IntrospectionException thrown on introspection of bean and if
    *    a property in beanProps does not map to a property of bean.
    */
   public static void mapJavaBeanProperties(Object bean, Properties beanProps, boolean isStrict)
      throws IntrospectionException
   {

      HashMap propertyMap = new HashMap();
      BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
      PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
      for (int p = 0; p < props.length; p++)
      {
         String fieldName = props[p].getName();
         propertyMap.put(fieldName, props[p]);
      }

      boolean trace = log.isTraceEnabled();
      Iterator keys = beanProps.keySet().iterator();
      if( trace )
         log.trace("Mapping properties for bean: "+bean);
      while( keys.hasNext() )
      {
         String name = (String) keys.next();
         String text = (String) beanProps.getProperty(name);
         PropertyDescriptor pd = (PropertyDescriptor) propertyMap.get(name);
         if (pd == null)
         {
            /* Try the property name with the first char uppercased to handle
            a property name like dLQMaxResent whose expected introspected
            property name would be DLQMaxResent since the JavaBean
            Introspector would view setDLQMaxResent as the setter for a
            DLQMaxResent property whose Introspector.decapitalize() method
            would also return "DLQMaxResent".
            */
            if( name.length() > 1 )
            {
               char first = name.charAt(0);
               String exName = Character.toUpperCase(first) + name.substring(1);
               pd = (PropertyDescriptor) propertyMap.get(exName);
            }

            if( pd == null )
            {
               if(isStrict)
               {
                  String msg = "No property found for: "+name+" on JavaBean: "+bean;
                  throw new IntrospectionException(msg);
               }
               else
               {
                  // since is not strict, ignore that this property was not found
                  continue;
               }
            }
         }
         Method setter = pd.getWriteMethod();
         if( trace )
            log.trace("Property editor found for: "+name+", editor: "+pd+", setter: "+setter);
         if (setter != null)
         {
            Class ptype = pd.getPropertyType();
            PropertyEditor editor = PropertyEditorManager.findEditor(ptype);
            if (editor == null)
            {
               if( trace )
                  log.trace("Failed to find property editor for: "+name);
            }
            try
            {
               editor.setAsText(text);
               Object args[] = {editor.getValue()};
               setter.invoke(bean, args);
            }
            catch (Exception e)
            {
               if( trace )
                  log.trace("Failed to write property", e);
            }
         }
      }
   }

   /**
    * Gets the package names that will be searched for property editors.
    *
    * @return   The package names that will be searched for property editors.
    */
   public String[] getEditorSearchPath()
   {
      return PropertyEditorManager.getEditorSearchPath();
   }

   /**
    * Sets the package names that will be searched for property editors.
    *
    * @param path   The serach path.
    */
   public void setEditorSearchPath(final String[] path)
   {
      PropertyEditorManager.setEditorSearchPath(path);
   }
}
