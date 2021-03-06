/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.common.util.concurrent;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.SuppressLoggerChecks;
import org.elasticsearch.common.component.Lifecycle;
import org.elasticsearch.test.ESTestCase;
import org.mockito.InOrder;

import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Tests {@link AbstractLifecycleRunnable}.
 */
public class AbstractLifecycleRunnableTests extends ESTestCase {
    private final Lifecycle lifecycle = mock(Lifecycle.class);
    private final Logger logger = mock(Logger.class);

    public void testDoRunOnlyRunsWhenNotStoppedOrClosed() throws Exception {
        Callable<?> runCallable = mock(Callable.class);

        // it's "not stopped or closed"
        when(lifecycle.stoppedOrClosed()).thenReturn(false);

        AbstractLifecycleRunnable runnable = new AbstractLifecycleRunnable(lifecycle, logger) {
            @Override
            public void onFailure(Exception e) {
                fail("It should not fail");
            }

            @Override
            protected void doRunInLifecycle() throws Exception {
                runCallable.call();
            }
        };

        runnable.run();

        InOrder inOrder = inOrder(lifecycle, logger, runCallable);

        inOrder.verify(lifecycle).stoppedOrClosed();
        inOrder.verify(runCallable).call();
        inOrder.verify(lifecycle).stoppedOrClosed(); // onAfter uses it too, but we're not testing it here
        inOrder.verifyNoMoreInteractions();
    }

    @SuppressLoggerChecks(reason = "mock usage")
    public void testDoRunDoesNotRunWhenStoppedOrClosed() throws Exception {
        Callable<?> runCallable = mock(Callable.class);

        // it's stopped or closed
        when(lifecycle.stoppedOrClosed()).thenReturn(true);

        AbstractLifecycleRunnable runnable = new AbstractLifecycleRunnable(lifecycle, logger) {
            @Override
            public void onFailure(Exception e) {
                fail("It should not fail");
            }

            @Override
            protected void doRunInLifecycle() throws Exception {
                fail("Should not run with lifecycle stopped or closed.");
            }
        };

        runnable.run();

        InOrder inOrder = inOrder(lifecycle, logger, runCallable);

        inOrder.verify(lifecycle).stoppedOrClosed();
        inOrder.verify(logger).trace(anyString());
        inOrder.verify(lifecycle).stoppedOrClosed(); // onAfter uses it too, but we're not testing it here
        inOrder.verifyNoMoreInteractions();
    }

    public void testOnAfterOnlyWhenNotStoppedOrClosed() throws Exception {
        Callable<?> runCallable = mock(Callable.class);
        Callable<?> afterCallable = mock(Callable.class);

        // it's "not stopped or closed"
        when(lifecycle.stoppedOrClosed()).thenReturn(false);

        AbstractLifecycleRunnable runnable = new AbstractLifecycleRunnable(lifecycle, logger) {
            @Override
            public void onFailure(Exception e) {
                fail("It should not fail");
            }

            @Override
            protected void doRunInLifecycle() throws Exception {
                runCallable.call();
            }

            @Override
            protected void onAfterInLifecycle() {
                try {
                    afterCallable.call();
                } catch (Exception e) {
                    fail("Unexpected for mock.");
                }
            }
        };

        runnable.run();

        InOrder inOrder = inOrder(lifecycle, logger, runCallable, afterCallable);

        inOrder.verify(lifecycle).stoppedOrClosed();
        inOrder.verify(runCallable).call();
        inOrder.verify(lifecycle).stoppedOrClosed();
        inOrder.verify(afterCallable).call();
        inOrder.verifyNoMoreInteractions();
    }

    public void testOnAfterDoesNotHappenWhenStoppedOrClosed() throws Exception {
        Callable<?> runCallable = mock(Callable.class);

        // it's stopped or closed
        when(lifecycle.stoppedOrClosed()).thenReturn(true);

        AbstractLifecycleRunnable runnable = new AbstractLifecycleRunnable(lifecycle, logger) {
            @Override
            public void onFailure(Exception e) {
                fail("It should not fail");
            }

            @Override
            protected void doRunInLifecycle() throws Exception {
                fail("Should not run with lifecycle stopped or closed.");
            }

            @Override
            protected void onAfterInLifecycle() {
                fail("Should not run with lifecycle stopped or closed.");
            }
        };

        runnable.run();

        InOrder inOrder = inOrder(lifecycle, runCallable);

        inOrder.verify(lifecycle, times(2)).stoppedOrClosed();
        inOrder.verifyNoMoreInteractions();
    }
}
