package org.apache.flume.interceptor;

import java.util.concurrent.TimeUnit;
import java.util.List;

import org.apache.flume.Context; 
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder; 
import org.apache.flume.interceptor.Interceptor; 


public class FlumeDelayInterceptor implements Interceptor {
 
    private Integer delay=1000;
 
    public FlumeDelayInterceptor(Integer delay){
        this.delay = delay;
    }

    @Override 
    public void initialize() { 
        // no-op 
    } 
 
    @Override
    public Event intercept(Event event) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
            // Sleep for the number of milliseconds
        } catch (InterruptedException e) {
            // Handle exception
        }
        return event;
    }
 
    @Override
    public List<Event> intercept(List<Event> events) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
            // Sleep for the number of milliseconds
        } catch (InterruptedException e) {
            // Handle exception
        }
        return events;
    }
 
    @Override
    public void close() {
        // no-op
    }
 
    public static class Builder implements Interceptor.Builder {
 
        private Integer delay;
 
        @Override
        public void configure(Context context) {
            // Gets the property from flume configuration
            delay = context.getInteger("interceptorDelay");
        }
 
        @Override
        public Interceptor build() {
            return new FlumeDelayInterceptor(delay);
        }
    }
}

