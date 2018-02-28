package com.sadakatsu.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;

import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class TimingTest {
    private static class ElapsedTimeMatcher extends BaseMatcher<Object> {
        @Override
        public boolean matches( Object value ) {
            return (
                value != null &&
                Double.class.equals(value.getClass()) &&
                Double.isFinite((Double) value) &&
                (Double) value >= 0.
            );
        }

        @Override
        public void describeTo( Description description ) {
            description.appendText("finite, positive double");
        }
    }
    
    private TestLogger logger;
    private ElapsedTimeMatcher elapsedTimeMatcher = new ElapsedTimeMatcher();
    
    @Before
    public void prepareLogger() {
        logger = TestLoggerFactory.getTestLogger(Time.class);
        logger.setEnabledLevels(Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR);
    }
    
    @After
    public void clearLoggers() {
        TestLoggerFactory.clear();
    }
    
    @Test
    public void timeCallsNiladicProcedure() throws Exception {
        NiladicProcedure mock = Mockito.mock(NiladicProcedure.class);
        Time.time(mock, "mock NiladicProcedure");
        Mockito.verify(mock).run();
    }
    
    @Test
    public void timeLogsSimpleMessageForNiladicProcedureSuccess() throws Exception {
        NiladicProcedure mock = Mockito.mock(NiladicProcedure.class);
        Time.time(mock, "mock NiladicProcedure");
        Mockito.verify(mock).run();
        assertLogLike("mock NiladicProcedure", true);
    }
    
    private void assertLogLike( String message, boolean callSucceeded ) {
        ImmutableList<LoggingEvent> events = logger.getLoggingEvents();
        Assert.assertEquals(1, events.size());
        
        LoggingEvent event = events.get(0);
        Assert.assertEquals(Level.DEBUG, event.getLevel());
        
        String actualMessage = event.getMessage();
        Assert.assertEquals("{} in {} ms", actualMessage);
        
        ImmutableList<Object> arguments = event.getArguments();
        Assert.assertEquals(2, arguments.size());
        
        String expectedFirstArgument = String.format(
            "%s %s",
            message,
            callSucceeded ? "returned" : "threw an Exception"
        );
        Assert.assertEquals(expectedFirstArgument, arguments.get(0));
        
        Assert.assertThat(arguments.get(1), elapsedTimeMatcher);
    }
    
    @Test
    public void timeLogsSimpleMessageForNiladicProcedureFailure() throws Exception {
        NiladicProcedure mock = Mockito.mock(NiladicProcedure.class);
        Mockito.doThrow(Exception.class).when(mock).run();
        
        try {
            Time.time(mock, "mock NiladicProcedure");
            Assert.fail("The procedure's Exception should have been thrown but was captured.");
        } catch (Exception e) {
            // Good! The exception was thrown.
        }
        
        Mockito.verify(mock).run();
        assertLogLike("mock NiladicProcedure", false);
    }
    
    @Test
    public void timeLogsVariadicMessageForNiladicProcedureSuccess() throws Exception {
        NiladicProcedure mock = Mockito.mock(NiladicProcedure.class);
        
        String format = "variadic test: %s, %d, %f, %c";
        Object[] arguments = new Object[] { "string", 1, 2.7, 'x' };
        Time.time(mock, format, arguments);
        Mockito.verify(mock).run();
        String expectedMessage = String.format(format, arguments);
        assertLogLike(expectedMessage, true);
    }
    
    @Test
    public void timeLogsVariadicMessageForNiladicProcedureFailure() throws Exception {
        NiladicProcedure mock = Mockito.mock(NiladicProcedure.class);
        Mockito.doThrow(Exception.class).when(mock).run();
        
        String format = "variadic test: %s, %d, %f, %c";
        Object[] arguments = new Object[] { "string", 1, 2.7, 'x' };
        try {
            Time.time(mock, format, arguments);
            Assert.fail("The procedure's Exception should have been thrown but was captured.");
        } catch (Exception e) {
            // Good! The exception was thrown.
        }
        
        Mockito.verify(mock).run();
        String expectedMessage = String.format(format, arguments);
        assertLogLike(expectedMessage, false);
    }
    
    @Test
    public void timeCallsNiladicFunctionAndReturnsResult() throws Exception {
        NiladicFunction<?> mock = Mockito.mock(NiladicFunction.class);
        Mockito.doReturn(true).when(mock).run();
        Object result = Time.time(mock, "mock NiladicFunction");
        Mockito.verify(mock).run();
        Assert.assertEquals(true, result);
    }
    
    @Test
    public void timeLogsSimpleMessageForNiladicFunctionSuccess() throws Exception {
        NiladicFunction<?> mock = Mockito.mock(NiladicFunction.class);
        Mockito.doReturn(256L).when(mock).run();
        Object result = Time.time(mock, "mock NiladicFunction");
        Mockito.verify(mock).run();
        assertLogLike("mock NiladicFunction", true);
        Assert.assertEquals(256L, result);
    }
    
    @Test
    public void timeLogsSimpleMessageForNiladicFunctionFailure() throws Exception {
        NiladicFunction<?> mock = Mockito.mock(NiladicFunction.class);
        Mockito.doThrow(Exception.class).when(mock).run();
        
        try {
            Time.time(mock, "mock NiladicFunction");
            Assert.fail("The function's Exception should have been thrown but was captured.");
        } catch (Exception e) {
            // Good! The exception was thrown.
        }
        
        Mockito.verify(mock).run();
        assertLogLike("mock NiladicFunction", false);
    }
    
    @Test
    public void timeLogsVariadicMessageForNiladicFunctionSuccess() throws Exception {
        NiladicFunction<?> mock = Mockito.mock(NiladicFunction.class);
        Mockito.doReturn(-44.).when(mock).run();
        
        String format = "variadic test: %x, %f, %c, %s";
        Object[] arguments = new Object[] { 255, -1.3, 'o', "teehee" };
        Object result = Time.time(mock, format, arguments);
        Mockito.verify(mock).run();
        Assert.assertEquals(-44., (Double) result, Double.MIN_NORMAL);
        String expectedMessage = String.format(format, arguments);
        assertLogLike(expectedMessage, true);
    }
    
    @Test
    public void timeLogsVariadicMessageForNiladicFunctionFailure() throws Exception {
        NiladicFunction<?> mock = Mockito.mock(NiladicFunction.class);
        Mockito.doThrow(Exception.class).when(mock).run();
        
        String format = "variadic test: %x, %f, %c, %s";
        Object[] arguments = new Object[] { 255, -1.3, 'o', "teehee" };
        try {
            Time.time(mock, format, arguments);
            Assert.fail("The function's Exception should have been thrown but was captured.");
        } catch (Exception e) {
            // Good! The exception was thrown.
        }
        
        Mockito.verify(mock).run();
        String expectedMessage = String.format(format, arguments);
        assertLogLike(expectedMessage, false);
    }
    
    @Test
    public void timeLogsNothingForNiladicProcedureWhenDebugIsDisabled() throws Exception {
        logger.setEnabledLevels(Level.INFO, Level.WARN, Level.ERROR);
        NiladicProcedure mock = Mockito.mock(NiladicProcedure.class);
        Time.time(mock, "should not be logged");
        Mockito.verify(mock).run();
        assertNoLoggingOccurred();
    }
    
    private void assertNoLoggingOccurred() {
        Assert.assertThat(
            logger.getLoggingEvents(),
            Matchers.is(Arrays.asList())
        );
    }
    
    @Test
    public void timeLogsNothingForNiladicFunctionWhenDebugIsDisabled() throws Exception {
        logger.setEnabledLevels(Level.INFO, Level.WARN, Level.ERROR);
        NiladicFunction<?> mock = Mockito.mock(NiladicFunction.class);
        Mockito.doReturn('q').when(mock).run();
        Object result = Time.time(mock, "should not be logged");
        Mockito.verify(mock).run();
        Assert.assertEquals('q', result);
        assertNoLoggingOccurred();
    }
    
    @Test
    public void doesNotExposeAnyConstructors() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor<?>[] constructors = Time.class.getConstructors();
        Assert.assertArrayEquals(new Constructor<?>[] {}, constructors);
        
        constructors = Time.class.getDeclaredConstructors();
        Assert.assertEquals(1, constructors.length);
        Assert.assertFalse(constructors[0].isAccessible());
        
        // Ugh.  If I want complete code coverage metrics, I need to call the constructor.
        constructors[0].setAccessible(true);
        constructors[0].newInstance();
    }
}
