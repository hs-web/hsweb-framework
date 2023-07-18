package org.hswebframework.web.crud.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public class TransactionUtils {

    public static Mono<Void> registerSynchronization(TransactionSynchronization synchronization,
                                                     Function<TransactionSynchronization, Mono<Void>> whenNoTransaction) {
        return TransactionSynchronizationManager
                .forCurrentTransaction()
                .doOnNext(manager -> manager.registerSynchronization(synchronization))
                .then()
                .onErrorResume(err -> {
                    log.warn("register TransactionSynchronization [{}] error", synchronization, err);
                    return whenNoTransaction.apply(synchronization);
                });
    }
}
