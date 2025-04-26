package com.plexpt.chatgpt.listener;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.plexpt.chatgpt.listener.ConsoleStreamListener;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachable;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class ConsoleStreamListenerTest {

    private ConsoleStreamListener consoleStreamListener;
    private ListAppender<ILoggingEvent> listAppender;

    @Before
    public void setUp() {
        // Create ConsoleStreamListener instance
        consoleStreamListener = new ConsoleStreamListener();

        // Get Logger for ConsoleStreamListener class
        Logger logger = (Logger) LoggerFactory.getLogger(ConsoleStreamListener.class);

        // Create a ListAppender to capture log events
        listAppender = new ListAppender<>();
        listAppender.start();

        // Attach the ListAppender to the Logger
        logger.addAppender(listAppender);
    }

    @Test
    public void testOnMsg() {
        // Test onMsg method
        String testMessage = "Hello, World!";

        // Call the onMsg method
        consoleStreamListener.onMsg(testMessage);

        // Get the captured log message
        ILoggingEvent loggingEvent = listAppender.list.get(0);

        // Validate the log content
        assertEquals(testMessage, loggingEvent.getMessage());
    }

    @Test
    public void testOnError() {
        // Test onError method
        Throwable testThrowable = new RuntimeException("Test Exception");
        String testResponse = "Test Response";

        // Call the onError method
        consoleStreamListener.onError(testThrowable, testResponse);

        // Get the captured log message for the error
        ILoggingEvent loggingEvent = listAppender.list.get(0);

        // Validate the log content contains the expected error message
        assertTrue(loggingEvent.getMessage().contains("ConsoleStreamListener error:"));

    }
}
