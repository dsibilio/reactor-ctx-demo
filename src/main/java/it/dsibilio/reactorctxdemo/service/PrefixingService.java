package it.dsibilio.reactorctxdemo.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

import static it.dsibilio.reactorctxdemo.constants.Constants.*;

@Service
public class PrefixingService {

    // Data propagation via Reactor Context is transparent, the method signature is unscathed!
    public Flux<String> doPrefix(Flux<Integer> toPrefix) {
        return toPrefix
                .flatMap(data -> 
                    Mono.subscriberContext()
                    .flatMap(ctx -> ctx.getOrDefault(PREFIX_KEY, Mono.just("")))
                    .map(prefix -> prefix + " " + data)
                );
    }
    
    // Data propagation via local variable pollutes the method signature!
    public Flux<String> doPrefix(String prefix, Flux<Integer> toPrefix) {
        return toPrefix
                .map(data -> prefix + " " + data);
    }

    // Data propagation via Tuples alters the method signature and makes the code ugly really quickly!
    public Flux<String> doPrefixWithTuple(Flux<Tuple2<Integer, Mono<SecurityContext>>> toPrefix) {
        return toPrefix
                .flatMap(tuple -> tuple.getT2().map(securityContext -> Tuples.of(tuple.getT1(), securityContext.getAuthentication().getName())))
                .map(tuple -> DATA_REQUESTED_BY + tuple.getT2() + " " + tuple.getT1());
    }
    
}
