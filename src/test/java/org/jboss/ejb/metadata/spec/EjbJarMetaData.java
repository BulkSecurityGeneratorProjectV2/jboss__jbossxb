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
package org.jboss.ejb.metadata.spec;

import org.jboss.javaee.metadata.support.IdMetaDataImplWithDescriptionGroup;

/**
 * EjbJarMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class EjbJarMetaData extends IdMetaDataImplWithDescriptionGroup
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 809339942454480150L;

   /** The ejb client jar */
   private String ejbClientJar;

   /** The enterprise beans */
   private EnterpriseBeansMetaData enterpriseBeans;

   /** The relations */
   private RelationsMetaData relationships;

   /** The assembly descriptor */
   private AssemblyDescriptorMetaData assemblyDescriptor;
   
   /**
    * Create a new EjbJarMetaData.
    */
   public EjbJarMetaData()
   {
      // For serialization
   }

   /**
    * Whether this is ejb1.x
    * 
    * @return true when ejb1.x
    */
   public boolean isEJB1x()
   {
      return false;
   }

   /**
    * Whether this is ejb2.x
    * 
    * @return true when ejb2.x
    */
   public boolean isEJB2x()
   {
      return false;
   }

   /**
    * Whether this is ejb2.1
    * 
    * @return true when ejb2.1
    */
   public boolean isEJB21()
   {
      return false;
   }

   /**
    * Whether this is ejb3.x
    * 
    * @return true when ejb3.x
    */
   public boolean isEJB3x()
   {
      return false;
   }
   
   /**
    * Get the ejbClientJar.
    * 
    * @return the ejbClientJar.
    */
   public String getEjbClientJar()
   {
      return ejbClientJar;
   }

   /**
    * Set the ejbClientJar.
    * 
    * @param ejbClientJar the ejbClientJar.
    * @throws IllegalArgumentException for a null ejbClientJar
    */
   public void setEjbClientJar(String ejbClientJar)
   {
      if (ejbClientJar == null)
         throw new IllegalArgumentException("Null ejbClientJar");
      this.ejbClientJar = ejbClientJar;
   }

   /**
    * Get the enterpriseBeans.
    * 
    * @return the enterpriseBeans.
    */
   public EnterpriseBeansMetaData getEnterpriseBeans()
   {
      return enterpriseBeans;
   }

   /**
    * Set the enterpriseBeans.
    * 
    * @param enterpriseBeans the enterpriseBeans.
    * @throws IllegalArgumentException for a null enterpriseBeans
    */
   public void setEnterpriseBeans(EnterpriseBeansMetaData enterpriseBeans)
   {
      if (enterpriseBeans == null)
         throw new IllegalArgumentException("Null enterpriseBeans");
      this.enterpriseBeans = enterpriseBeans;
      enterpriseBeans.setEjbJarMetaData(this);
   }

   /**
    * Get the relationships.
    * 
    * @return the relationships.
    */
   public RelationsMetaData getRelationships()
   {
      return relationships;
   }

   /**
    * Set the relationships.
    * 
    * @param relationships the relationships.
    * @throws IllegalArgumentException for a null relationships
    */
   public void setRelationships(RelationsMetaData relationships)
   {
      if (relationships == null)
         throw new IllegalArgumentException("Null relationships");
      this.relationships = relationships;
   }

   /**
    * Get the assemblyDescriptor.
    * 
    * @return the assemblyDescriptor.
    */
   public AssemblyDescriptorMetaData getAssemblyDescriptor()
   {
      return assemblyDescriptor;
   }

   /**
    * Set the assemblyDescriptor.
    * 
    * @param assemblyDescriptor the assemblyDescriptor.
    * @throws IllegalArgumentException for a null assemblyDescriptor
    */
   public void setAssemblyDescriptor(AssemblyDescriptorMetaData assemblyDescriptor)
   {
      if (assemblyDescriptor == null)
         throw new IllegalArgumentException("Null assemblyDescriptor");
      this.assemblyDescriptor = assemblyDescriptor;
   }
}