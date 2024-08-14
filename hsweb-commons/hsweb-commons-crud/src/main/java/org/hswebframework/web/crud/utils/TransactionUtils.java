package org.hswebframework.web.crud.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.reactive.TransactionContextManager;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public class TransactionUtils {

    public static Mono<Void> afterCommit(Mono<Void> task) {
        return TransactionUtils.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                @NonNull
                public Mono<Void> afterCommit() {
                    return task;
                }
            },
            TransactionSynchronization::afterCommit
        );
    }

    public static Mono<Void> registerSynchronization(TransactionSynchronization synchronization,
                                                     Function<TransactionSynchronization, Mono<Void>> whenNoTransaction) {
        return TransactionSynchronizationManager
            .forCurrentTransaction()
            .flatMap(manager -> {
                if (manager.isSynchronizationActive()) {
                    try {
                        manager.registerSynchronization(synchronization);
                    } catch (Throwable err) {
                        log.warn("register TransactionSynchronization [{}] error", synchronization, err);
                        return whenNoTransaction.apply(synchronization);
                    }
                    return Mono.empty();
                } else {
                    log.info("transaction is not active,execute TransactionSynchronization [{}] immediately.", synchronization);
                    return whenNoTransaction.apply(synchronization);
                }
            })
            .onErrorResume(NoTransactionException.class, err -> whenNoTransaction.apply(synchronization));
    }
}
