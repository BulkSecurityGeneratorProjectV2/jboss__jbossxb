/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util.file;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.File;
import java.io.IOException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 *
 **/
public class JarArchiveBrowser implements Iterator
{
   ZipFile zip;
   Enumeration entries;
   ZipEntry next;
   ArchiveBrowser.Filter filter;

   public JarArchiveBrowser(File f, ArchiveBrowser.Filter filter)
   {
      this.filter = filter;
      try
      {
         zip = new ZipFile(f);
         entries = zip.entries();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      setNext();
   }

   public boolean hasNext()
   {
      return next != null;
   }

   private void setNext()
   {
      next = null;
      while (entries.hasMoreElements() && next == null)
      {
         do
         {
            next = (ZipEntry) entries.nextElement();
         } while (entries.hasMoreElements() && next.isDirectory());
         if (next.isDirectory()) next = null;

         if (next != null && !filter.accept(next.getName()))
         {
            next = null;
         }
      }
   }

   public Object next()
   {
      ZipEntry entry = next;
      setNext();

      try
      {
         return zip.getInputStream(entry);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void remove()
   {
      throw new RuntimeException("Illegal operation on ArchiveBrowser");
   }
}