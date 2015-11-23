/*
 * Copyright (c) 2010-2014. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.spring.unitofwork;

import org.axonframework.common.Assert;
import org.axonframework.messaging.unitofwork.Transaction;
import org.axonframework.messaging.unitofwork.TransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * TransactionManager implementation that uses a {@link org.springframework.transaction.PlatformTransactionManager} as
 * underlying transaction manager.
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class SpringTransactionManager implements TransactionManager {

    private PlatformTransactionManager transactionManager;
    private TransactionDefinition transactionDefinition;

    /**
     * @param transactionManager    The transaction manager to use
     * @param transactionDefinition The definition for transactions to create
     */
    public SpringTransactionManager(PlatformTransactionManager transactionManager,
                                    TransactionDefinition transactionDefinition) {
        Assert.notNull(transactionManager, "transactionManager may not be null");
        this.transactionManager = transactionManager;
        this.transactionDefinition = transactionDefinition;
    }

    /**
     * Initializes the SpringTransactionManager with the given <code>transactionManager</code> and the default
     * transaction definition.
     *
     * @param transactionManager the transaction manager to use
     */
    public SpringTransactionManager(PlatformTransactionManager transactionManager) {
        this(transactionManager, new DefaultTransactionDefinition());
    }

    /**
     * Default constructor. Requires the transaction manager to be set using setter injection.
     */
    public SpringTransactionManager() {
        this.transactionDefinition = new DefaultTransactionDefinition();
    }

    @Override
    public Transaction startTransaction() {
        TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
        return new Transaction() {
            @Override
            public void commit() {
                commitTransaction(status);
            }

            @Override
            public void rollback() {
                rollbackTransaction(status);
            }
        };
    }

    /**
     * Commits the transaction with given <code>status</code> if the transaction is new and not completed.
     *
     * @param status The status of the transaction to commit
     */
    protected void commitTransaction(TransactionStatus status) {
        if (status.isNewTransaction() && !status.isCompleted()) {
            transactionManager.commit(status);
        }
    }

    /**
     * Rolls back the transaction with given <code>status</code> if the transaction is new and not completed.
     *
     * @param status The status of the transaction to roll back
     */
    protected void rollbackTransaction(TransactionStatus status) {
        if (status.isNewTransaction() && !status.isCompleted()) {
            transactionManager.rollback(status);
        }
    }

    /**
     * The PlatformTransactionManager that manages the transactions with the underlying data source.
     *
     * @param transactionManager the transaction manager that manages transactions with underlying data sources
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * The TransactionDefinition to use by the transaction manager. Defaults to a {@link
     * org.springframework.transaction.support.DefaultTransactionDefinition}.
     *
     * @param transactionDefinition the TransactionDefinition to use by the transaction manager
     */
    public void setTransactionDefinition(TransactionDefinition transactionDefinition) {
        this.transactionDefinition = transactionDefinition;
    }
}
