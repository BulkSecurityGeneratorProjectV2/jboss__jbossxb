/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.net.protocol.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.security.Permission;
import java.io.FilePermission;

/**
 * Provides local file access via URL semantics, correctly returning
 * the last modified time of the underlying file.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class FileURLConnection
   extends URLConnection
{
   protected File file;

   public FileURLConnection(final URL url)
      throws MalformedURLException, IOException
   {
      super(url);

      file = new File(url.getPath().replace('/', File.separatorChar).replace('|', ':'));

      doOutput = false;
   }

   /**
    * Returns the underlying file for this connection.
    */
   public File getFile() {
      return file;
   }

   /**
    * Checks if the underlying file for this connection exists.
    *
    * @throws FileNotFoundException
    */
   public void connect() throws IOException {
      if (connected) return;

      if (!file.exists()) {
         throw new FileNotFoundException(file.getPath());
      }

      connected = true;
   }

   public InputStream getInputStream() throws IOException {
      if (!connected) connect();

      return new FileInputStream(file);
   }

   public OutputStream getOutputStream() throws IOException {
      if (!connected) connect();

      return new FileOutputStream(file);
   }

   /**
    * Provides support for returning the value for the
    * <tt>last-modified</tt> header.
    */
   public String getHeaderField(final String name) {
      if (name.equalsIgnoreCase("last-modified")) {
         return String.valueOf(getLastModified());
      }

      return super.getHeaderField(name);
   }

   /** 
    * Return a permission for both read & write since both
    * input and output streams are supported.
    */
   public Permission getPermission() throws IOException
   {
      return new FilePermission(file.getPath(), "read,write");
   }

   /**
    * Returns the last modified time of the underlying file.
    */
   public long getLastModified() {
      return file.lastModified();
   }
}