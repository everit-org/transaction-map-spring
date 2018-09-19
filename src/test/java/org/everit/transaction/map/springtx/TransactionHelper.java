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

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class TransactionHelper {

  private final PlatformTransactionManager transactionManager;

  public TransactionHelper() {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setUrl("jdbc:h2:mem:test");
    this.transactionManager = new DataSourceTransactionManager(dataSource);
  }

  private void doWithPropagation(final Runnable action, final int propagation) {
    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.setPropagationBehavior(propagation);
    template.execute((status) -> {
      action.run();
      return null;
    });
  }

  public void notSupported(final Runnable action) {
    doWithPropagation(action, TransactionTemplate.PROPAGATION_NOT_SUPPORTED);
  }

  public void required(final Runnable action) {
    doWithPropagation(action, TransactionTemplate.PROPAGATION_REQUIRED);
  }

  public void requiresNew(final Runnable action) {
    doWithPropagation(action, TransactionTemplate.PROPAGATION_REQUIRES_NEW);
  }
}
