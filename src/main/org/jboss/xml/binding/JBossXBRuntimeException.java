/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xml.binding;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class JBossXBRuntimeException
   extends RuntimeException
{
   public JBossXBRuntimeException()
   {
   }

   public JBossXBRuntimeException(String message)
   {
      super(message);
   }

   public JBossXBRuntimeException(Throwable cause)
   {
      super(cause);
   }

   public JBossXBRuntimeException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
