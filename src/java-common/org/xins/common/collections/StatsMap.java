/*
 * $Id: StatsMap.java,v 1.1 2010/10/25 20:36:51 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Map that remembers which properties have not been accessed.
 *
 * @version $Revision: 1.1 $ $Date: 2010/10/25 20:36:51 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0.0
 */
public class StatsMap<K, V> extends AbstractMap<K, V> {

   private Map<K, V> sourceMap;

   private Map<K, V> unused;

   public StatsMap(Map<K, V> source) {
      sourceMap = source;
      unused = new HashMap<K, V>();
      unused.putAll(source);
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      return sourceMap.entrySet();
   }

   @Override
   public V get(Object key) {
      unused.remove(key);
      return sourceMap.get(key);
   }

   /**
    * Retrieves the set of unused entries.
    *
    * @return
    *    a {@link Map} containing which were not queried, never <code>null</code>.
    */
   public Map<K, V> getUnused() {
      return Collections.unmodifiableMap(unused);
   }
}
