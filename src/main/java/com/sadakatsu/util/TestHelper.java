package com.sadakatsu.util;

import static org.junit.Assert.fail;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class TestHelper {
	private TestHelper() {}
	
    public static <T> void failForReturn( String method, String exception, T returned, Object...arguments ) {
        StringBuilder builder = startFailureMessage(method, exception, arguments);
        builder.append("returned ");
        builder.append(returned);
        builder.append(".");
        
        String message = builder.toString();
        fail(message);
    }
    
    private static StringBuilder startFailureMessage( String method, String exception, Object...arguments ) {
        StringBuilder builder = new StringBuilder(method);
        builder.append("(");
        for (int i = 0, length = arguments.length; i < length; ++i) {
            if (i > 0) {
                builder.append(", ");
            }
            Object argument = arguments[i];
            if (isArray(argument)) {
            	String representation = getArrayString(argument);
            	builder.append(representation);
            } else {
            	builder.append(argument);
            }
        }
        builder.append(") should have thrown ");
        builder.append(exception);
        builder.append(", but instead ");
        return builder;
    }
    
    private static boolean isArray( Object argument ) {
    	return argument != null && argument.getClass().isArray();
    }
    
    private static String getArrayString( Object array ) {
    	List<String> items = new ArrayList<>();
    	
    	int length = Array.getLength(array);
    	for (int i = 0; i < length; ++i) {
    		Object item = Array.get(array, i);
    		
    		String string;
    		if (isArray(item)) {
    			string = getArrayString(item);
    		} else if (item != null) {
    			string = item.toString();
    		} else {
    			string = "null";
    		}
    		
    		items.add(string);
    	}
    	
    	return "[ " + StringUtils.join(items, ", ") + " ]";
    }
    
    public static void failForWrongThrowable(
        String method,
        String expectedException,
        Throwable actualException,
        Object...arguments
    ) {
        StringBuilder builder = startFailureMessage(method, expectedException, arguments);
        builder.append("threw ");
        builder.append(actualException.getClass().getSimpleName());
        builder.append(": ");
        builder.append(actualException.getMessage());
        
        String message = builder.toString();
        fail(message);
    }
    
    public static String getDefaulToString( Object object ) {
        if (object == null) {
            return "null";
        }
        return object.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(object));
    }
}
