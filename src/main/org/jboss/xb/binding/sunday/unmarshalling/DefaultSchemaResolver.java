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
package org.jboss.xb.binding.sunday.unmarshalling;

import java.io.InputStream;
import java.net.URL;

import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.logging.Logger;
import org.xml.sax.InputSource;
import org.w3c.dom.ls.LSInput;

/**
 * A default SchemaBindingResolver that uses a JBossEntityResolver to locate
 * the schema xsd.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class DefaultSchemaResolver implements SchemaBindingResolver
{
   private static Logger log = Logger.getLogger(DefaultSchemaResolver.class);

   private String baseURI;
   private JBossEntityResolver resolver;

   public DefaultSchemaResolver()
   {
      this(new JBossEntityResolver());
   }

   public DefaultSchemaResolver(JBossEntityResolver resolver)
   {
      this.resolver = resolver;
   }

   public String getBaseURI()
   {
      return baseURI;
   }

   public void setBaseURI(String baseURI)
   {
      this.baseURI = baseURI;
   }

   /**
    * Uses the JBossEntityResolver.resolveEntity by:
    * 
    * 1. Using the nsUri as the systemID
    * 2. Using the schemaLocation as the systemID
    * 3. If that fails, the baseURI is not null, the xsd is located using URL(baseURL, schemaLocation)
    * 4. If the baseURI is null, the xsd is located using URL(schemaLocation)
    */
   public SchemaBinding resolve(String nsURI, String baseURI, String schemaLocation)
   {
      InputSource is = getInputSource(nsURI, baseURI, schemaLocation);

      SchemaBinding schema = null;
      if (is != null)
      {
         schema = XsdBinder.bind(is.getByteStream(), null, baseURI);
      }
      return schema;
   }

   public LSInput resolveAsLSInput(String nsURI, String baseURI, String schemaLocation)
   {
      LSInput lsInput = null;
      InputSource is = getInputSource(nsURI, baseURI, schemaLocation);
      if (is != null)
      {
         String publicId = is.getPublicId();
         String systemId = is.getSystemId();
         lsInput = new LSInputAdaptor(publicId, systemId, baseURI);
         lsInput.setCharacterStream(is.getCharacterStream());
         lsInput.setByteStream(is.getByteStream());
         lsInput.setEncoding(is.getEncoding());
      }
      return lsInput;
   }

   private InputSource getInputSource(String nsURI, String baseURI, String schemaLocation)
   {
      boolean trace = log.isTraceEnabled();
      InputSource is = null;

      // First try to resolve the namespace as a systemID
      try
      {
         is = resolver.resolveEntity(null, nsURI);
      }
      catch (Exception e)
      {
         if (trace)
            log.trace("Failed to use nsUri as systemID", e);
      }

      if (is == null && schemaLocation != null)
      {
         // Next try the schemaLocation as a systemID
         try
         {
            is = resolver.resolveEntity(null, schemaLocation);
         }
         catch (Exception e)
         {
            if (trace)
               log.trace("Failed to use schemaLocation as systemID", e);
         }

         if (is == null)
         {
            // Just try resolving the schemaLocation against the baseURI
            try
            {
               if (baseURI == null)
               {
                  baseURI = this.baseURI;
               }

               URL schemaURL = null;
               if (baseURI != null)
               {
                  URL baseURL = new URL(baseURI);
                  schemaURL = new URL(baseURL, schemaLocation);
               }
               else
               {
                  schemaURL = new URL(schemaLocation);
               }

               if (schemaURL != null)
               {
                  InputStream is2 = schemaURL.openStream();
                  is = new InputSource(is2);
               }
            }
            catch (Exception e)
            {
               if (trace)
                  log.trace("Failed to use schemaLocation as URL", e);
            }
         }
      }
      return is;
   }
}
