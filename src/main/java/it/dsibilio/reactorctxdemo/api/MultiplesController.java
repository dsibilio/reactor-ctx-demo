package it.dsibilio.reactorctxdemo.api;

import it.dsibilio.reactorctxdemo.service.PrefixingService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.function.Tuples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static it.dsibilio.reactorctxdemo.constants.Constants.*;
import static java.time.Duration.*;

@Controller
@RequestMapping("/multiples")
public class MultiplesController {
    @Autowired
    private PrefixingService service;

    @GetMapping(value = "/{base}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<String>>> getMultiples(
            @PathVariable int base,
            @RequestParam int multiplier) {

        Flux<String> body = 
                service.doPrefix(getMultiplierFlux(base, multiplier))
                .onErrorReturn(IllegalArgumentException.class, ILLEGAL_ARGUMENT_MSG)
                .subscriberContext(ctx -> ctx.put(PREFIX_KEY, getPrefix(ctx)));

        return Mono.just(ResponseEntity.ok().body(body));
    }

    private Mono<String> getPrefix(Context ctx) {
        return ctx.getOrDefault(SecurityContext.class, Mono.just(new SecurityContextImpl()))
                .map(securityCtx -> DATA_REQUESTED_BY + securityCtx.getAuthentication().getName());
    }

    @GetMapping(value = "/localvar/{base}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<String>>> getMultiplesWithLocalVarPrefix(
            @PathVariable int base,
            @RequestParam int multiplier,
            Authentication authentication) {

        String prefix = DATA_REQUESTED_BY + authentication.getName();

        Flux<String> body = 
                service.doPrefix(prefix, getMultiplierFlux(base, multiplier))
                .onErrorReturn(IllegalArgumentException.class, ILLEGAL_ARGUMENT_MSG);

        return Mono.just(ResponseEntity.ok().body(body));
    }

    @GetMapping(value = "/tuples/{base}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<String>>> getMultiplesWithTuplesPrefix(
            @PathVariable int base,
            @RequestParam int multiplier) {

        Flux<String> body = 
                service.doPrefixWithTuple(
                            getMultiplierFlux(base, multiplier)
                            .map(multiple -> Tuples.of(multiple, ReactiveSecurityContextHolder.getContext()))
                        )
                .onErrorReturn(IllegalArgumentException.class, ILLEGAL_ARGUMENT_MSG);

        return Mono.just(ResponseEntity.ok().body(body));
    }

    private Flux<Integer> getMultiplierFlux(int base, int multiplier) {
        return Flux
                .<Integer>create(sink -> {
                    if(base < 0 || multiplier < 0)
                        sink.error(new IllegalArgumentException());

                    for(int i = 1; i <= 10; i++)
                        sink.next(base * multiplier * i);

                    sink.complete();
                })
                .delayElements(ofSeconds(1));
    }

}
