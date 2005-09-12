/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xb.binding.sunday.unmarshalling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.xml.sax.Attributes;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public abstract class ModelGroupBinding
   implements ParticleBinding, Cloneable
{
   protected int minOccurs;
   protected int maxOccurs;
   protected boolean maxOccursUnbounded;

   public abstract ElementBinding getArrayItem();

   public abstract void addElement(ElementBinding element);

   public abstract void addModelGroup(ModelGroupBinding modelGroup);

   public abstract void setWildcard(WildcardBinding binding);

   public int getMinOccurs()
   {
      return minOccurs;
   }

   public int getMaxOccurs()
   {
      return maxOccurs;
   }

   public boolean getMaxOccursUnbounded()
   {
      return maxOccursUnbounded;
   }

   /**
    * This method is not actually used during parsing. It's here only for internal tests.
    *
    * @param qName an element name
    * @return true if the model group may start with the specified element
    */
   public boolean mayStartWith(QName qName)
   {
      return mayStartWith(qName, Collections.EMPTY_SET);
   }

   public abstract Cursor newCursor();

   public Object clone() throws CloneNotSupportedException
   {
      return super.clone();
   }

   // Protected

   protected abstract boolean mayStartWith(QName qName, Set set);

   // Inner

   public static abstract class Cursor
   {
      protected static final byte ELEMENT_STATUS_STARTED = 1;
      protected static final byte ELEMENT_STATUS_FINISHED = 2;
      protected static final byte ELEMENT_STATUS_UNINITIALIZED = 4;

      protected final ModelGroupBinding group;
      protected byte elementStatus = ELEMENT_STATUS_UNINITIALIZED;

      protected Cursor(ModelGroupBinding theGroup)
      {
         this.group = theGroup;
      }

      public ModelGroupBinding getModelGroup()
      {
         return group;
      }

      public boolean isElementFinished()
      {
         return (elementStatus & ELEMENT_STATUS_FINISHED) > 0;
      }

      public abstract ParticleBinding getCurrentParticle();

      public abstract ElementBinding getElement();

      public List startElement(QName qName, Attributes attrs)
      {
         return startElement(qName, attrs, Collections.EMPTY_SET, Collections.EMPTY_LIST, true);
      }

      public ElementBinding getElement(QName qName, Attributes attrs)
      {
         return getElement(qName, attrs, Collections.EMPTY_SET);
      }

      public abstract void endElement(QName qName);

      // Protected

      protected abstract List startElement(QName qName,
                                           Attributes atts,
                                           Set passedGroups,
                                           List groupStack,
                                           boolean required);

      protected abstract ElementBinding getElement(QName qName, Attributes atts, Set passedGroups);

      protected ElementBinding getElement(List group, QName qName, Attributes atts, Set passedGroups)
      {
         ElementBinding element = null;
         for(int i = 0; i < group.size(); ++i)
         {
            Object item = group.get(i);
            if(item instanceof ElementBinding)
            {
               ElementBinding choice = (ElementBinding)item;
               if(qName.equals(choice.getQName()))
               {
                  element = choice;
                  break;
               }
            }
            else if(item instanceof ModelGroupBinding)
            {
               ModelGroupBinding modelGroup = (ModelGroupBinding)item;
               if(!passedGroups.contains(modelGroup))
               {
                  switch(passedGroups.size())
                  {
                     case 0:
                        passedGroups = Collections.singleton(this);
                        break;
                     case 1:
                        passedGroups = new HashSet(passedGroups);
                     default:
                        passedGroups.add(this);
                  }

                  ElementBinding e = modelGroup.newCursor().getElement(qName, atts, passedGroups);
                  if(e != null)
                  {
                     element = e;
                     if(!qName.equals(e.getQName()))
                     {
                        throw new JBossXBRuntimeException(
                           "There is a bug in ModelGroupBinding.Cursor.getElement(QName,Attributes) impl"
                        );
                     }
                     break;
                  }
               }
            }
            else if(item instanceof WildcardBinding)
            {
               WildcardBinding wildcard = (WildcardBinding)item;
               ElementBinding e = wildcard.getElement(qName, atts);
               if(e != null)
               {
                  element = e;
                  if(!qName.equals(e.getQName()))
                  {
                     throw new JBossXBRuntimeException(
                        "There is a bug in ModelGroupBinding.Cursor.getElement(QName,Attributes) impl"
                     );
                  }
                  break;
               }
            }
         }
         return element;
      }

      protected List addItem(List list, Object o)
      {
         switch(list.size())
         {
            case 0:
               list = Collections.singletonList(o);
               break;
            case 1:
               list = new ArrayList(list);
            default:
               list.add(o);
         }
         return list;
      }
   }
}