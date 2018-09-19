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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.everit.transaction.map.TransactionalMap;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Map that manages its transactional state by enlisting itself if there is an ongoing transaction.
 * The implementation calls the functions of a wrapped {@link TransactionalMap}.
 *
 * @param <K>
 *          the type of keys maintained by this map
 * @param <V>
 *          the type of mapped values
 */
public class SpringTxMap<K, V> implements Map<K, V> {

  /**
   * Instance of class is registered to Spring TransactionSynchronizationManager. Follows the
   * lifecycle of the Spring transaction and calls the necessary functions on the wrapped
   * transactional map.
   */
  private class MapTransactionSynchronization implements TransactionSynchronization {

    private final Long transactionId;

    MapTransactionSynchronization(final Long transactionId) {
      this.transactionId = transactionId;
    }

    @Override
    public void afterCommit() {
      wrapped.commitTransaction();
    }

    @Override
    public void afterCompletion(final int status) {
      if (TransactionSynchronization.STATUS_COMMITTED != status) {
        wrapped.rollbackTransaction();
      }
    }

    @Override
    public void resume() {
      wrapped.resumeTransaction(transactionId);
    }

    @Override
    public void suspend() {
      wrapped.suspendTransaction();
    }
  }

  private static final AtomicLong TRANSACTION_COUNTER = new AtomicLong();

  protected final TransactionalMap<K, V> wrapped;

  /**
   * Constructor.
   *
   * @param transactionalMap
   *          The Map that should is managed by this class.
   * @throws NullPointerException
   *           if transactionalMap or transactionManager is null.
   */
  public SpringTxMap(final TransactionalMap<K, V> transactionalMap) {
    Objects.requireNonNull(transactionalMap, "Transactional map cannot be null");
    this.wrapped = transactionalMap;

  }

  @Override
  public void clear() {
    updateTransactionState();
    wrapped.clear();
  }

  @Override
  public boolean containsKey(final Object key) {
    updateTransactionState();
    return wrapped.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    updateTransactionState();
    return wrapped.containsValue(value);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    updateTransactionState();
    return wrapped.entrySet();
  }

  @Override
  public V get(final Object key) {
    updateTransactionState();
    return wrapped.get(key);
  }

  @Override
  public boolean isEmpty() {
    updateTransactionState();
    return wrapped.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    updateTransactionState();
    return wrapped.keySet();
  }

  @Override
  public V put(final K key, final V value) {
    updateTransactionState();
    return wrapped.put(key, value);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    updateTransactionState();
    wrapped.putAll(m);
  }

  @Override
  public V remove(final Object key) {
    updateTransactionState();
    return wrapped.remove(key);
  }

  @Override
  public int size() {
    updateTransactionState();
    return wrapped.size();
  }

  /**
   * Checks if the Map is associated to a transaction and manages its context based on the current
   * Transaction state.
   */
  protected void updateTransactionState() {
    boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
    if (transactionActive) {
      Object associatedTransaction = wrapped.getAssociatedTransaction();
      if (associatedTransaction == null) {
        Long transactionId = TRANSACTION_COUNTER.incrementAndGet();

        TransactionSynchronization transactionSynchronization =
            new MapTransactionSynchronization(transactionId);

        TransactionSynchronizationManager.registerSynchronization(transactionSynchronization);

        wrapped.startTransaction(transactionId);
      }
    }
  }

  @Override
  public Collection<V> values() {
    updateTransactionState();
    return wrapped.values();
  }

}
