/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util.state;

/** An exception thrown when an invalid transition is attempted from a state.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class IllegalTransitionException extends Exception
{
   public IllegalTransitionException(String msg)
   {
      super(msg);
   }
}
