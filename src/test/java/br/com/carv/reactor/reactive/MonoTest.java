package br.com.carv.reactor.reactive;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MonoTest {

    /**
     * Reactive Streams
     * 1. Asynchronous
     * 2. Non-blocking
     * 3. Backpressure
     *
     *
     * Interfaces:
     * 1. Publisher - Emit the events. Cold: If no one subscribes, it will not issue anything.
     * 2. Subscription is created
     * 3. Publisher (onSubscribe with the subscription) -> Subscriber
     * 4. Subscription <- (request N) Subscriber
     * 5. Publisher -> (onNext) Subscriber
     *
     *  until:
     *      1. Publisher sends all the objects requested.
     *      2. Publisher sends all the objects it has. (onComplete) subscriber and subscription will be canceled.
     *      3. There is an error. (onError) -> subscriber and subscription will be canceled.
     *
     */

    private Logger logger = Logger.getLogger(MonoTest.class.getSimpleName());

    @Test
    public void monoSubscriber() {
        String name = "João Gabriel";
        Mono<String> mono = Mono.just(name).log();

        mono.subscribe();

        logger.info("-------------------------------------------------------------------------------------------");

        StepVerifier.create(mono).expectNext(name).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "João Gabriel";
        Mono<String> mono = Mono.just(name).log();

        mono.subscribe(value -> logger.info("Value: " + value));

        logger.info("-------------------------------------------------------------------------------------------");

        StepVerifier.create(mono).expectNext(name).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "João Gabriel";
        Mono<String> mono = Mono.just(name).map(value -> {  throw new RuntimeException("Testing mono with error");  });


        mono.subscribe(value -> logger.info("Value: " + value),
                value -> logger.log(Level.SEVERE,"Something bad happened"));

        mono.subscribe(value -> logger.info("Value: " + value), Throwable::printStackTrace);

        logger.info("-------------------------------------------------------------------------------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class).verify();

    }

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "João Gabriel";
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase);

        mono.subscribe(value -> logger.info("Value: " + value), Throwable::printStackTrace,
                () -> logger.info("Finished!"));

        logger.info("-------------------------------------------------------------------------------------------");

        StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "João Gabriel";
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase);

        mono.subscribe(value -> logger.info("Value: " + value), Throwable::printStackTrace,
                () -> logger.info("Finished!"), Subscription::cancel);

        logger.info("-------------------------------------------------------------------------------------------");

        StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerSubscriptionRequest() {
        String name = "João Gabriel";
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase);

        mono.subscribe(value -> logger.info("Value: " + value), Throwable::printStackTrace,
                () -> logger.info("Finished!"), subscription -> subscription.request(2L));

        logger.info("-------------------------------------------------------------------------------------------");

        StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();

    }

    @Test
    public void monoDoOnMethods() {
        String name = "João Gabriel";
        Mono<String> mono =
                Mono.just(name)
                        .log()
                        .map(String::toUpperCase)
                                .doOnSubscribe(subscription -> logger.info("Subscription"))
                                        .doOnRequest(longNumber -> logger.info("Request Received. Starting doing something..."))
                                                .doOnNext(s -> logger.info("Value is here. Executing doOnNext: " + s))
                                                        .doOnSuccess(s -> logger.info("doOnSuccess executed"));

        mono.subscribe(value -> logger.info("Value: " + value), Throwable::printStackTrace,
                () -> logger.info("Finished!"), subscription -> subscription.request(2L));

    }

    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .doOnError(e -> logger.info("Error Message: " + e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();

    }

    @Test
    public void monoDoErrorResume() {
        String name = "João Gabriel";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .doOnError(e -> logger.info("Error Message: " + e.getMessage()))
                .onErrorResume(s -> {
                    logger.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();

    }
}
