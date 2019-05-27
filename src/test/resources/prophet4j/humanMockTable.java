/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.core.client.mock;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.admin.TimeType;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorUtil;
import org.apache.accumulo.core.security.TablePermission;

public class MockTable {
  
  static class MockMemKey extends Key {
    private int count;
    
    MockMemKey(Key key, int count) {
      super(key);
      this.count = count;
    }
    
    @Override
    public int hashCode() {
      return super.hashCode() + count;
    }
    
    @Override
    public boolean equals(Object obj) {
      MockMemKey other = (MockMemKey) obj;
      return super.equals(other) && count == other.count;
    }
    
    @Override
    public String toString() {
      return super.toString() + " count=" + count;
    }
    
    @Override
    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
        return compare;
      if (o instanceof MockMemKey) {
        MockMemKey other = (MockMemKey) o;
        if (count < other.count)
          return 1;
        if (count > other.count)
          return -1;
      } else {
        return 1;
      }
      return 0;
    }
  };
  
  final SortedMap<Key,Value> table = new ConcurrentSkipListMap<Key,Value>();
  int mutationCount = 0;
  final Map<String,String> settings;
  Map<String,EnumSet<TablePermission>> userPermissions = new HashMap<String,EnumSet<TablePermission>>();
  private TimeType timeType;
  
  MockTable(boolean useVersions, TimeType timeType) {
    this.timeType = timeType;
    settings = IteratorUtil.generateInitialTableProperties();
    for (Entry<String,String> entry : AccumuloConfiguration.getDefaultConfiguration()) {
      String key = entry.getKey();
      if (key.startsWith(Property.TABLE_PREFIX.getKey()))
        settings.put(key, entry.getValue());
    }
  }
  
  synchronized void addMutation(Mutation m) {
    long now = System.currentTimeMillis();
    mutationCount++;
    for (ColumnUpdate u : m.getUpdates()) {
      Key key = new Key(m.getRow(), 0, m.getRow().length, u.getColumnFamily(), 0, u.getColumnFamily().length, u.getColumnQualifier(), 0,
          u.getColumnQualifier().length, u.getColumnVisibility(), 0, u.getColumnVisibility().length, u.getTimestamp());
      if (u.isDeleted())
        key.setDeleted(true);
      if (!u.hasTimestamp())
        if (timeType.equals(TimeType.LOGICAL))
          key.setTimestamp(mutationCount);
        else
          key.setTimestamp(now);
      
      table.put(new MockMemKey(key, mutationCount), new Value(u.getValue()));
    }
  }
  
  /**
   * @deprecated since 1.4 {@link #attachIterator(String, IteratorSetting)}
   */
  public void addAggregators(List<? extends org.apache.accumulo.core.iterators.conf.PerColumnIteratorConfig> aggregators) {
    for (Entry<String,String> entry : IteratorUtil.generateAggTableProperties(aggregators).entrySet()) {
      String key = entry.getKey();
      if (key.startsWith(Property.TABLE_PREFIX.getKey()))
        settings.put(key, entry.getValue());
    }
  }
}
