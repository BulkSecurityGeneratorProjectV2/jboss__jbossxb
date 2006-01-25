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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.namespace.QName;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.xml.sax.Attributes;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class ChoiceBinding
   extends ModelGroupBinding
{
   private List choices = Collections.EMPTY_LIST;

   public ChoiceBinding(SchemaBinding schema)
   {
      super(schema);
   }

   public ElementBinding getArrayItem()
   {
      return null;
   }

   public void addParticle(ParticleBinding particle)
   {
      switch(choices.size())
      {
         case 0:
            choices = Collections.singletonList(particle);
            break;
         case 1:
            choices = new ArrayList(choices);
         default:
            choices.add(particle);
      }

      super.addParticle(particle);
   }

   public Collection getParticles()
   {
      return Collections.unmodifiableCollection(choices);
   }

   public Cursor newCursor(ParticleBinding particle)
   {
      return new Cursor(particle)
      {
         private int pos = -1;
         private ElementBinding element;
         private int occurence;

         public ParticleBinding getCurrentParticle()
         {
            if(pos < 0)
            {
               throw new JBossXBRuntimeException(
                  "The cursor has not been positioned yet! startElement should be called."
               );
            }
            return (ParticleBinding)choices.get(pos);
         }

         public ElementBinding getElement()
         {
            if(pos < 0)
            {
               throw new JBossXBRuntimeException(
                  "The cursor has not been positioned yet! startElement should be called."
               );
            }
            return element;
         }

         public void endElement(QName qName)
         {
            if(element == null || !element.getQName().equals(qName))
            {
               throw new JBossXBRuntimeException("Failed to process endElement for " + qName +
                  " since the current element is " + (element == null ? "null" : element.getQName().toString())
               );
            }

            if(log.isTraceEnabled())
            {
               log.trace("endElement " + qName + " in " + getModelGroup());
            }
         }

         public int getOccurence()
         {
            return occurence;
         }

         protected List startElement(QName qName, Attributes atts, Set passedGroups, List groupStack, boolean required)
         {
            if(log.isTraceEnabled())
            {
               StringBuffer sb = new StringBuffer();
               sb.append("startElement " + qName + " in " + getModelGroup() + ", " + choices.size() + ": ");

               for(int i = 0; i < choices.size(); ++i)
               {
                  ParticleBinding particle = (ParticleBinding)choices.get(i);
                  Object o = particle.getTerm();
                  if(o instanceof ElementBinding)
                  {
                     sb.append(((ElementBinding)o).getQName());
                  }
                  else if(o instanceof SequenceBinding)
                  {
                     sb.append("sequence");
                  }
                  else if(o instanceof ChoiceBinding)
                  {
                     sb.append("choice");
                  }
                  else if(o instanceof AllBinding)
                  {
                     sb.append("all");
                  }

                  sb.append(" ");
               }
               sb.append("]");
               log.trace(sb.toString());
            }

            int i = pos;
            if(pos >= 0)
            {
               ParticleBinding particle = getCurrentParticle();
               if(particle.getMaxOccursUnbounded() ||
                  occurence < particle.getMinOccurs() ||
                  occurence < particle.getMaxOccurs())
               {
                  --i;
               }
            }

            // i update pos only if the element has been found, though it seems to be irrelevant
            // since the cursor is going to be thrown away in case the element has not been found
            while(i < choices.size() - 1)
            {
               ParticleBinding particle = (ParticleBinding)choices.get(++i);
               Object item = particle.getTerm();
               if(item instanceof ElementBinding)
               {
                  ElementBinding element = (ElementBinding)item;
                  if(qName.equals(element.getQName()))
                  {
                     if(pos == i)
                     {
                        ++occurence;
                     }
                     else
                     {
                        pos = i;
                        occurence = 1;
                     }
                     groupStack = addItem(groupStack, this);
                     this.element = element;

                     if(log.isTraceEnabled())
                     {
                        log.trace("found " + qName + " in " + getModelGroup());
                     }
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

                     int groupStackSize = groupStack.size();
                     groupStack = modelGroup.newCursor(particle).startElement(
                        qName, atts, passedGroups, groupStack, particle.isRequired(occurence)
                     );

                     if(groupStackSize != groupStack.size())
                     {
                        if(pos != i)
                        {
                           pos = i;
                           occurence = 1;
                        }
                        else
                        {
                           ++occurence;
                        }
                        groupStack = addItem(groupStack, this);
                        element = null;
                        break;
                     }
                  }
               }
               else if(item instanceof WildcardBinding)
               {
                  WildcardBinding wildcard = (WildcardBinding)item;
                  element = wildcard.getElement(qName, atts);
                  if(element != null)
                  {
                     if(pos != i)
                     {
                        pos = i;
                        occurence = 1;
                     }
                     else
                     {
                        ++occurence;
                     }
                     groupStack = addItem(groupStack, this);
                     break;
                  }
               }
            }

            if(log.isTraceEnabled())
            {
               if(i == choices.size())
               {
                  log.trace(qName + " not found in " + getModelGroup());
               }
               else
               {
                  log.trace("leaving " + getModelGroup() + " i=" + i + ", pos=" + pos);
               }
            }

            return groupStack;
         }

         protected ElementBinding getElement(QName qName, Attributes atts, Set passedGroups, boolean ignoreWildcards)
         {
            return getElement(choices, qName, atts, passedGroups, ignoreWildcards);
         }
      };
   }

   protected boolean mayStartWith(QName qName, Set set)
   {
      boolean result = false;
      for(int i = 0; i < choices.size(); ++i)
      {
         ParticleBinding particle = (ParticleBinding)choices.get(i);
         Object item = particle.getTerm();
         if(item instanceof ElementBinding)
         {
            ElementBinding element = (ElementBinding)item;
            if(qName.equals(element.getQName()))
            {
               result = true;
               break;
            }
         }
         else if(item instanceof ModelGroupBinding)
         {
            ModelGroupBinding modelGroup = (ModelGroupBinding)item;
            if(!set.contains(modelGroup))
            {
               switch(set.size())
               {
                  case 0:
                     set = Collections.singleton(this);
                     break;
                  case 1:
                     set = new HashSet(set);
                  default:
                     set.add(this);
               }

               result = modelGroup.mayStartWith(qName, set);

               if(result)
               {
                  break;
               }
            }
         }
         else
         {
            throw new JBossXBRuntimeException("Unexpected item type in model group: " + item);
         }
      }

      return result;
   }
}
