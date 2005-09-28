/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.xb.binding.sunday.unmarshalling;

import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.NamespaceRegistry;
import org.jboss.xb.binding.Util;
import org.jboss.xb.binding.metadata.CharactersMetaData;
import org.jboss.xb.binding.metadata.ValueMetaData;
import org.jboss.xb.binding.parser.JBossXBParser;
import org.xml.sax.Attributes;

/**
 * todo: to improve performance, consider gathering all the necessary binding metadata in startElement and
 * pushing it instead of ElementBinding into elementStack to free the endElement implementation from
 * re-gathering this same metadata again.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class SundayContentHandler
   implements JBossXBParser.ContentHandler
{
   private final static Logger log = Logger.getLogger(SundayContentHandler.class);

   private final static Object NIL = new Object();

   private final SchemaBinding schema;
   private final SchemaBindingResolver schemaResolver;

   private StringBuffer textContent = new StringBuffer();
   private final StackImpl stack = new StackImpl();

   private Object root;
   private NamespaceRegistry nsRegistry = new NamespaceRegistry();

   public SundayContentHandler(SchemaBinding schema)
   {
      this.schema = schema;
      this.schemaResolver = null;
   }

   public SundayContentHandler(SchemaBindingResolver schemaResolver)
   {
      this.schemaResolver = schemaResolver;
      this.schema = null;
   }

   public void characters(char[] ch, int start, int length)
   {
      // todo: textContent should be an instvar of StackItem
      if(((StackItem)stack.peek()).particle != null)
      {
         textContent.append(ch, start, length);
      }
   }

   public void endElement(String namespaceURI, String localName, String qName)
   {
      ElementBinding elementBinding = null;
      QName endName = null;
      StackItem item = (StackItem)stack.pop();
      while(true)
      {
         if(item.particle != null)
         {
            elementBinding = (ElementBinding)item.particle.getTerm();
            break;
         }

         if(stack.isEmpty())
         {
            break;
         }

         if(endName == null)
         {
            endName = localName.length() == 0 ? new QName(qName) : new QName(namespaceURI, localName);
         }

         item = endParticle(item, endName);
         pop();
      }

      if(elementBinding == null)
      {
         throw new JBossXBRuntimeException("Failed to endElement " + qName + ": binding not found");
      }

      endName = elementBinding.getQName();
      if(!endName.getLocalPart().equals(localName) || !endName.getNamespaceURI().equals(namespaceURI))
      {
         throw new JBossXBRuntimeException("Failed to end element " +
            new QName(namespaceURI, localName) +
            ": element on the stack is " + endName
         );
      }

      endElement(item.o, item.particle);

      // if parent group is choice, it should also be finished
      if(!stack.isEmpty() && ((StackItem)stack.peek()).cursor.getParticle().getTerm() instanceof ChoiceBinding)
      {
         endParticle(pop(), endName);
      }
   }

   public void startElement(String namespaceURI,
                            String localName,
                            String qName,
                            Attributes atts,
                            XSTypeDefinition type)
   {
      QName startName = localName.length() == 0 ? new QName(qName) : new QName(namespaceURI, localName);
      ParticleBinding particle = null;
      SchemaBinding schemaBinding = schema;

      if(stack.isEmpty())
      {
         if(schemaBinding != null)
         {
            particle = schemaBinding.getElementParticle(startName);
         }
         else if(schemaResolver != null)
         {
            String schemaLocation = atts == null ? null : Util.getSchemaLocation(atts, namespaceURI);
            schemaBinding = schemaResolver.resolve(namespaceURI, null, schemaLocation);
            if(schemaBinding != null)
            {
               particle = schemaBinding.getElementParticle(startName);
            }
         }
         else
         {
            throw new JBossXBRuntimeException("Neither schema binding nor schema binding resolver is available!");
         }
      }
      else
      {
         StackItem item = (StackItem)stack.peek();
         if(item.particle != null)
         {
            TermBinding term = item.particle.getTerm();
            ElementBinding element = (ElementBinding)term;
            ParticleBinding typeParticle = element.getType().getParticle();
            ModelGroupBinding modelGroup = typeParticle == null ?
               null :
               (ModelGroupBinding)typeParticle.getTerm();
            if(modelGroup == null)
            {
               throw new JBossXBRuntimeException("Element " + element.getQName() + " should be of a complex type!");
            }

            ModelGroupBinding.Cursor cursor = modelGroup.newCursor(typeParticle);
            List newCursors = cursor.startElement(startName, atts);
            if(newCursors.isEmpty())
            {
               // todo: it was a warning
               throw new JBossXBRuntimeException(
                  startName + " not found as a child of " + ((ElementBinding)term).getQName()
               );
            }
            else
            {
               // push all except the last one
               for(int i = newCursors.size() - 1; i >= 0; --i)
               {
                  cursor = (ModelGroupBinding.Cursor)newCursors.get(i);

                  ParticleBinding modelGroupParticle = cursor.getParticle();
                  ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
                  Object o = handler.startParticle(item.o, startName, modelGroupParticle, atts, nsRegistry);

                  push(cursor, o);
               }
               particle = cursor.getCurrentParticle();
            }
         }
         else
         {
            while(!stack.isEmpty())
            {
               if(item.particle != null)
               {
                  TermBinding term = item.particle.getTerm();
                  ElementBinding element = (ElementBinding)term;
                  ParticleBinding typeParticle = element.getType().getParticle();
                  ModelGroupBinding modelGroup = typeParticle == null ?
                     null :
                     (ModelGroupBinding)typeParticle.getTerm();
                  if(modelGroup == null)
                  {
                     throw new JBossXBRuntimeException(
                        "Element " + element.getQName() + " should be of a complex type!"
                     );
                  }

                  /*
                  if(!typeParticle.isRepeatable())
                  {
                     throw new JBossXBRuntimeException("Particle is not repeatable for " + startName);
                  } */

                  ModelGroupBinding.Cursor cursor = modelGroup.newCursor(typeParticle);
                  List newCursors = cursor.startElement(startName, atts);
                  if(newCursors.isEmpty())
                  {
                     // todo: it was a warning
                     throw new JBossXBRuntimeException(
                        startName + " not found as a child of " + ((ElementBinding)term).getQName()
                     );
                  }
                  else
                  {
                     // push all except the last one
                     for(int i = newCursors.size() - 1; i >= 0; --i)
                     {
                        cursor = (ModelGroupBinding.Cursor)newCursors.get(i);

                        ParticleBinding modelGroupParticle = cursor.getParticle();
                        ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
                        Object o = handler.startParticle(item.o, startName, modelGroupParticle, atts, nsRegistry);

                        push(cursor, o);
                     }
                     particle = cursor.getCurrentParticle();
                  }
                  break;
               }
               else
               {
                  ModelGroupBinding.Cursor cursor = item.cursor;
                  int currentOccurence = cursor.getOccurence();
                  List newCursors = cursor.startElement(startName, atts);
                  if(newCursors.isEmpty())
                  {
                     pop();
                     item = endParticle(item, startName);
                  }
                  else
                  {
                     if(cursor.getOccurence() - currentOccurence > 0)
                     {
                        item = endParticle(item, startName);

                        ParticleBinding modelGroupParticle = cursor.getParticle();
                        ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
                        Object o = handler.startParticle(item.o, startName, modelGroupParticle, atts, nsRegistry);

                        item = push(cursor, o);
                     }

                     // push all except the last one
                     for(int i = newCursors.size() - 2; i >= 0; --i)
                     {
                        cursor = (ModelGroupBinding.Cursor)newCursors.get(i);

                        ParticleBinding modelGroupParticle = cursor.getParticle();
                        ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
                        Object o = handler.startParticle(item.o, startName, modelGroupParticle, atts, nsRegistry);

                        push(cursor, o);
                     }
                     particle = ((ModelGroupBinding.Cursor)newCursors.get(0)).getCurrentParticle();
                     break;
                  }
               }
            }
         }
      }

      Object o = null;
      if(particle != null)
      {
         Object parent = stack.isEmpty() ? null : ((StackItem)stack.peek()).o;
         if(particle.getTerm() instanceof WildcardBinding)
         {
            WildcardBinding wildcard = (WildcardBinding)particle.getTerm();
            ElementBinding element = wildcard.getElement(startName, atts);
            if(element == null)
            {
               throw new JBossXBRuntimeException("Failed to resolve element " +
                  startName + " for wildcard."
               );
            }
            // todo
            particle = new ParticleBinding(element);
         }

         o = startElement(parent, particle, atts);
      }
      else
      {
         ElementBinding parentBinding = null;
         if(!stack.isEmpty())
         {
            ParticleBinding stackParticle = ((StackItem)stack.peek()).particle;
            if(stackParticle != null)
            {
               parentBinding = (ElementBinding)stackParticle.getTerm();
            }
         }

         if(parentBinding != null && parentBinding.getSchema() != null)
         {
            schemaBinding = parentBinding.getSchema();
         }

         String msg = "Element " +
            startName +
            " is not bound " +
            (parentBinding == null ? "as a global element." : "in type " + parentBinding.getType().getQName());
         if(schemaBinding != null && schemaBinding.isStrictSchema())
         {
            throw new JBossXBRuntimeException(msg);
         }
         else if(log.isTraceEnabled())
         {
            log.trace(msg);
         }
      }

      push(startName, particle, o);
   }

   private StackItem endParticle(StackItem item, QName qName)
   {
      ParticleBinding modelGroupParticle = item.cursor.getParticle();
      ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
      Object o = handler.endParticle(item.o, qName, modelGroupParticle);

      // model group should always have parent particle
      item = (StackItem)stack.peek();
      if(item.o != null)
      {
         ParticleBinding parentParticle = item.particle;
         if(parentParticle == null)
         {
            parentParticle = item.cursor.getParticle();
         }
         handler.setParent(item.o, o, qName, modelGroupParticle, parentParticle);
      }

      if(item.particle == null && item.cursor.getParticle().getTerm() instanceof ChoiceBinding)
      {
         item = endParticle(pop(), qName);
      }

      return item;
   }

   public void startPrefixMapping(String prefix, String uri)
   {
      nsRegistry.addPrefixMapping(prefix, uri);
   }

   public void endPrefixMapping(String prefix)
   {
      nsRegistry.removePrefixMapping(prefix);
   }

   public void processingInstruction(String target, String data)
   {
   }

   public Object getRoot()
   {
      return root;
   }

   // Private

   private Object startElement(Object parent, ParticleBinding particle, Attributes atts)
   {
      ElementBinding element = (ElementBinding)particle.getTerm();
      QName startName = element.getQName();
      TypeBinding type = element.getType();

      List elementHandlers = element.getInterceptors();
      for(int i = 0; i < elementHandlers.size(); ++i)
      {
         ElementInterceptor interceptor = (ElementInterceptor)elementHandlers.get(i);
         parent = interceptor.startElement(parent, startName, type);
         push(startName, particle, parent);
         interceptor.attributes(parent, startName, type, atts, nsRegistry);
      }

      Object o;
      // todo xsi:nil handling
      String nil = atts.getValue("xsi:nil");
      if(nil == null || !("1".equals(nil) || "true".equals(nil)))
      {
         o = type.getHandler().startParticle(parent, startName, particle, atts, nsRegistry);
      }
      else
      {
         o = NIL;
      }
      return o;
   }

   private void endElement(Object o, ParticleBinding particle)
   {
      ElementBinding element = (ElementBinding)particle.getTerm();
      QName endName = element.getQName();
      TypeBinding type = element.getType();
      List interceptors = element.getInterceptors();

      if(o != NIL)
      {
         //
         // characters
         //

         CharactersHandler simpleType = type.getSimpleType();
         if(textContent.length() > 0 || simpleType != null)
         {
            String dataContent;
            SchemaBinding schema = element.getSchema();
            if(textContent.length() == 0)
            {
               dataContent = null;
            }
            else
            {

               dataContent = textContent.toString();
               if(schema != null && schema.isReplacePropertyRefs())
               {
                  dataContent = StringPropertyReplacer.replaceProperties(dataContent);
               }
               textContent.delete(0, textContent.length());
            }

            Object unmarshalled;

            if(simpleType == null)
            {
               if(schema != null && schema.isStrictSchema())
               {
                  throw new JBossXBRuntimeException("Element " +
                     endName +
                     " type binding " +
                     type.getQName() +
                     " does not include text content binding ('" + dataContent
                  );
               }
               unmarshalled = dataContent;
            }
            else
            {
               ValueMetaData valueMetaData = element.getValueMetaData();
               if(valueMetaData == null)
               {
                  CharactersMetaData charactersMetaData = type.getCharactersMetaData();
                  if(charactersMetaData != null)
                  {
                     valueMetaData = charactersMetaData.getValue();
                  }
               }

               // todo valueMetaData is available from type
               unmarshalled = dataContent == null ?
                  simpleType.unmarshalEmpty(endName, type, nsRegistry, valueMetaData) :
                  simpleType.unmarshal(endName, type, nsRegistry, valueMetaData, dataContent);
            }

            if(unmarshalled != null)
            {
               // if startElement returned null, we use characters as the object for this element
               // todo subject to refactoring
               if(o == null)
               {
                  o = unmarshalled;
               }
               else if(simpleType != null)
               {
                  simpleType.setValue(endName, element, o, unmarshalled);
               }
            }

            // todo interceptors get dataContent?
            int i = interceptors.size();
            while(i-- > 0)
            {
               ElementInterceptor interceptor = (ElementInterceptor)interceptors.get(i);
               interceptor.characters(((StackItem)stack.peek(interceptors.size() - 1 - i)).o,
                  endName, type, nsRegistry, dataContent
               );
            }
         }
      }
      else
      {
         o = null;
      }

      //
      // endElement
      //

      Object parent = stack.isEmpty() ? null : ((StackItem)stack.peek()).o;
      o = type.getHandler().endParticle(o, endName, particle);

      int i = interceptors.size();
      while(i-- > 0)
      {
         ElementInterceptor interceptor = (ElementInterceptor)interceptors.get(i);
         interceptor.endElement(((StackItem)stack.peek(interceptors.size() - 1 - i)).o, endName, type);
      }

      //
      // setParent
      //

      i = interceptors.size();
      // todo yack...
      if(i == 0)
      {
         ParticleBinding parentParticle = null;
         for(int j = 0; j < stack.size(); ++j)
         {
            ParticleBinding peeked = ((StackItem)stack.peek(j)).particle;
            if(peeked != null && peeked.getTerm() instanceof ElementBinding)
            {
               parentParticle = peeked;
               break;
            }
         }

         if(parent != null)
         {
            /*if(o == null)
            {
               throw new JBossXBRuntimeException(endName + " is null!");
            } */
            type.getHandler().setParent(parent, o, endName, particle, parentParticle);
         }
         else if(parentParticle != null &&
            ((ElementBinding)parentParticle.getTerm()).getType().hasWildcard() &&
            !stack.isEmpty()/*!objectStack.isEmpty()*/)
         {
            // todo: review this> the parent has anyType, so it gets the value of its child
            for(int j = 0; j < stack.size(); ++j)
            {
               StackItem peeked = (StackItem)stack.peek(j);
               peeked.o = o;
               if(peeked.particle != null)
               {
                  //System.out.println("Value of " + endName + " " + o + " is promoted as the value of its parent " +
                  //   ((ElementBinding)peeked.particle).getQName());
                  break;
               }
            }

            if(log.isTraceEnabled())
            {
               log.trace("Value of " + endName + " " + o + " is promoted as the value of its parent element.");
            }
         }
      }
      else
      {
         while(i-- > 0)
         {
            ElementInterceptor interceptor = (ElementInterceptor)interceptors.get(i);
            parent = ((StackItem)stack.pop()).o;
            interceptor.add(parent, o, endName);
            o = parent;
         }
      }

      if(stack.isEmpty())
      {
         root = o;
      }
   }

   private void push(QName qName, ParticleBinding particle, Object o)
   {
      StackItem item = new StackItem(null, particle, o);
      stack.push(item);
      if(log.isTraceEnabled())
      {
         log.trace("pushed " + qName + "=" + o + ", binding=" + particle.getTerm());
      }
   }

   private StackItem push(ModelGroupBinding.Cursor cursor, Object o)
   {
      StackItem item = new StackItem(cursor, null, o);
      stack.push(item);
      if(log.isTraceEnabled())
      {
         log.trace("pushed cursor " + cursor);
      }
      return item;
   }

   private StackItem pop()
   {
      StackItem item = (StackItem)stack.pop();
      if(log.isTraceEnabled())
      {
         if(item.particle != null)
         {
            log.trace("poped " + ((ElementBinding)item.particle.getTerm()).getQName() + "=" + item.particle);
         }
         else
         {
            log.trace("poped " + item.cursor.getCurrentParticle().getTerm());
         }
      }
      return item;
   }

   // Inner

   private static class StackItem
   {
      final ModelGroupBinding.Cursor cursor;
      final ParticleBinding particle;
      Object o;

      public StackItem(ModelGroupBinding.Cursor cursor, ParticleBinding particle, Object o)
      {
         this.cursor = cursor;
         this.particle = particle;
         this.o = o;
      }
   }

   static class StackImpl
   {
      private LinkedList list = new LinkedList();

      public void clear()
      {
         list.clear();
      }

      public void push(Object o)
      {
         list.addLast(o);
      }

      public Object pop()
      {
         return list.removeLast();
      }

      public Object peek()
      {
         return list.getLast();
      }

      public Object peek(int i)
      {
         return list.get(list.size() - 1 - i);
      }

      public boolean isEmpty()
      {
         return list.isEmpty();
      }

      public int size()
      {
         return list.size();
      }
   }
}
