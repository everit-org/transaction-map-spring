/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.transaction.map.springtx;

import java.util.Map;

import org.everit.transaction.map.readcommited.ReadCommitedTransactionalMap;
import org.everit.transaction.map.springtx.SpringTxMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AppConfig.class })
public class SpringTxMapTest {

  @Autowired
  TransactionHelper tr;

  @Test
  public void testRollback() {
    Map<String, String> managedMap = new SpringTxMap<>(
        new ReadCommitedTransactionalMap<>(null));

    managedMap.put("key0", "value0");
    tr.required(() -> {
      managedMap.put("key1", "value1");
      try {
        tr.requiresNew(() -> {
          managedMap.put("key1", "otherValue");
          throw new NumberFormatException();
        });
      } catch (NumberFormatException e) {
        Assert.assertEquals(0, e.getSuppressed().length);
        Assert.assertEquals("value1", managedMap.get("key1"));
      }
    });
    Assert.assertEquals(2, managedMap.size());
  }

  @Test
  public void testSuspendedTransactions() {
    Map<String, String> managedMap = new SpringTxMap<>(
        new ReadCommitedTransactionalMap<>(null));
    managedMap.put("test1", "value1");
    tr.required(() -> {
      Assert.assertEquals("value1", managedMap.get("test1"));
      managedMap.put("test2", "value2");
      tr.requiresNew(() -> {
        Assert.assertFalse(managedMap.containsKey("test2"));
        managedMap.put("test3", "value3");
        tr.notSupported(() -> {
          Assert.assertFalse(managedMap.containsKey("test3"));
          Assert.assertFalse(managedMap.containsKey("test2"));
        });
      });
      Assert.assertTrue(managedMap.containsKey("test2"));
      Assert.assertTrue(managedMap.containsKey("test3"));
    });
  }
}
