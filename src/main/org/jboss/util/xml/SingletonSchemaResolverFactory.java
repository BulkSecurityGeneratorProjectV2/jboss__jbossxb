/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.util.xml;

import org.jboss.logging.Logger;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;

/**
 * SingletonSchemaResolverFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
public class SingletonSchemaResolverFactory implements SchemaResolverFactory
{
   /** The log */
   private static final Logger log = Logger.getLogger(SingletonSchemaResolverFactory.class);
   
   /** The factory instance */
   private static final SingletonSchemaResolverFactory singleton = new SingletonSchemaResolverFactory();
   
   /** The resolver */
   private final DefaultSchemaResolver resolver = new DefaultSchemaResolver();
   
   /**
    * Get the factory instance
    * 
    * @return the instance
    */
   public static SingletonSchemaResolverFactory getInstance()
   {
      return singleton;
   }

   /**
    * Create a new SingletonSchemaResolverFactory.
    */
   private SingletonSchemaResolverFactory()
   {
      addSchema("urn:jboss:bean-deployer", "org.jboss.kernel.plugins.deployment.xml.BeanSchemaInitializer");
      addSchema("urn:jboss:aop-deployer", "org.jboss.aspects.kernel.schema.AspectSchemaBindingInitalizer");
   }
   
   public SchemaBindingResolver getSchemaBindingResolver()
   {
      return resolver;
   }

   /**
    * Add a schema
    * 
    * @param namespace the namespace
    * @param initializer the initializer
    * @return true when added
    */
   protected boolean addSchema(String namespace, String initializer)
   {
      try
      {
         resolver.addSchemaInitializer(namespace, initializer);
         log.debug("Mapped initializer '" + namespace + "' to '" + initializer + "'");
         return true;
      }
      catch (Exception ignored)
      {
         log.trace("Ignored: ", ignored);
         return false;
      }
   }

   /**
    * Add a schema
    * 
    * @param namespace the namespace
    * @param initializer the initializer
    * @param location the location
    * @return true when added
    */
   protected boolean addSchema(String namespace, String initializer, String location)
   {
      if (addSchema(namespace, initializer) == false)
         return false;
      resolver.addSchemaLocation(namespace, location);
      log.debug("Mapped location '" + namespace + "' to '" + location + "'");
      return true;
   }
}
