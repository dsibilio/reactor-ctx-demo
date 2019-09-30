package it.dsibilio.reactorctxdemo.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import it.dsibilio.reactorctxdemo.api.MultiplesController;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import static it.dsibilio.reactorctxdemo.constants.Constants.*;
import static java.text.MessageFormat.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@WithMockUser(username = "user", password = "password", roles = "USER")
class MultiplesControllerTests {

    @Autowired
    private MultiplesController controller;

    @Test
    public void basicMultiplierTest() {
        VirtualTimeScheduler.getOrSet();

        List<String> expected = getExpected(5, 5);

        StepVerifier
        .create(controller.getMultiples(5, 5))
        .assertNext(isExpectedResponse(expected))
        .verifyComplete();
    }

    @Test
    public void basicMultiplierIllegalArgumentsTest() {
        StepVerifier
        .create(controller.getMultiples(-1, -1))
        .assertNext(isErrorResponse())
        .verifyComplete();
    }

    @Test
    public void localVarMultiplierTest() {
        VirtualTimeScheduler.getOrSet();

        List<String> expected = getExpected(5, 5);

        StepVerifier
        .create(controller.getMultiplesWithLocalVarPrefix(5, 5, SecurityContextHolder.getContext().getAuthentication()))
        .assertNext(isExpectedResponse(expected))
        .verifyComplete();
    }

    @Test
    public void localVarMultiplierIllegalArgumentsTest() {
        StepVerifier
        .create(controller.getMultiplesWithLocalVarPrefix(-1, -1, SecurityContextHolder.getContext().getAuthentication()))
        .assertNext(isErrorResponse())
        .verifyComplete();
    }

    @Test
    public void tupleMultiplierTest() {
        VirtualTimeScheduler.getOrSet();

        List<String> expected = getExpected(5, 5);

        StepVerifier
        .create(controller.getMultiplesWithTuplesPrefix(5, 5))
        .assertNext(isExpectedResponse(expected))
        .verifyComplete();
    }

    @Test
    public void tupleMultiplierIllegalArgumentsTest() {
        StepVerifier
        .create(controller.getMultiplesWithTuplesPrefix(-1, -1))
        .assertNext(isErrorResponse())
        .verifyComplete();
    }

    private Consumer<? super ResponseEntity<Flux<String>>> isExpectedResponse(List<String> expected) {
        return response -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());

            StepVerifier
            .withVirtualTime(() -> response.getBody())
            .expectSubscription()
            .thenAwait(Duration.ofSeconds(10))
            .expectNextSequence(expected)
            .verifyComplete();
        };
    }

    private Consumer<? super ResponseEntity<Flux<String>>> isErrorResponse() {
        return response -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());

            StepVerifier.create(response.getBody())
            .expectNext(ILLEGAL_ARGUMENT_MSG)
            .verifyComplete();
        };
    }

    private List<String> getExpected(int base, int multiplier) {
        List<String> expected = new ArrayList<>();
        for(int i=1; i<=10; i++)
            expected.add(format("{0}{1} {2}", DATA_REQUESTED_BY, "user", (base * multiplier * i)));

        return expected;
    }

}
