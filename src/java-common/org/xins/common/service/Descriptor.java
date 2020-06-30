/*
 * $Id: Descriptor.java,v 1.21 2012/03/03 10:41:19 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.service;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Descriptor for a service or group of services.
 *
 * @version $Revision: 1.21 $ $Date: 2012/03/03 10:41:19 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public abstract class Descriptor implements Serializable, Iterable<TargetDescriptor> {

   /**
    * Constructs a new <code>Descriptor</code>.
    */
   Descriptor() {

      // empty
   }

   /**
    * Checks if this descriptor denotes a group of descriptor of descriptorss.
    *
    * @return
    *    <code>true</code> if this descriptor denotes a group,
    *    <code>false</code> otherwise.
    */
   public abstract boolean isGroup();

   /**
    * Iterates over all leaves, the target descriptors.
    *
    * <p>The returned {@link Iterator} will not support
    * {@link Iterator#remove()}. The iterator will only return
    * {@link TargetDescriptor} instances, no instances of other classes and
    * no <code>null</code> values.
    *
    * <p>Also, this iterator is guaranteed to return {@link #getTargetCount()}
    * instances of class {@link TargetDescriptor}.
    *
    * @return
    *    iterator over the leaves, the target descriptors, in this
    *    descriptor, in the correct order, never <code>null</code>.
    * @deprecated use the {@link #iterator()} method or even better the for each loop.
    */
   @Deprecated
   public abstract Iterator iterateTargets();

   /**
    * Counts the total number of target descriptors in/under this descriptor.
    *
    * @return
    *    the total number of target descriptors, always &gt;= 1.
    */
   public abstract int getTargetCount();

   /**
    * Returns the <code>TargetDescriptor</code> that matches the specified
    * CRC-32 checksum.
    *
    * @param crc
    *    the CRC-32 checksum.
    *
    * @return
    *    the {@link TargetDescriptor} that matches the specified checksum, or
    *    <code>null</code>, if none could be found in this descriptor.
    */
   public abstract TargetDescriptor getTargetByCRC(int crc);
}
