package com.simple.crud.demo.config;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class ServiceLoggingAspect {

    // Logger instance for this aspect - used to output log messages (final means it's immutable)
    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAspect.class);
    
    // List of parameter name patterns that contain sensitive data (passwords, secrets, etc.) - these will be masked in logs
    private static final List<String> SENSITIVE_PATTERNS = List.of(
        "password", 
        "secret", 
        "token", 
        "credential", 
        "apikey", 
        "authorization"
    );

    @Pointcut("execution(public * com.simple.crud.demo.service..*(..))")
    public void serviceMethods() {}


    // @Around annotation - defines advice that executes before and after the target method
    // This advice wraps the execution of all methods matched by the serviceMethods() pointcut
    @Around("serviceMethods()")
    // Method that logs method execution details - takes ProceedingJoinPoint to control method invocation
    // Throws Throwable because the intercepted method might throw any exception
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // used to calculate method execution duration
        long startTime = System.currentTimeMillis();
        
        // Cast the join point's signature to MethodSignature to access method-specific information
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // Get the actual Method object from the signature - provides access to method metadata
        Method method = signature.getMethod();
        // Get the simple class name (without package) of the target object being intercepted
        String className = joinPoint.getTarget().getClass().getSimpleName();
        // Get the name of the method being executed
        String methodName = method.getName();
        
        // Format the method parameters into a readable string - sanitizes sensitive data and handles special types
        String params = formatParameters(signature.getParameterNames(), joinPoint.getArgs());
        // Log method entry at DEBUG level with class name, method name, and formatted parameters
        // → symbol indicates method entry
        log.debug("→ {}.{}({})", className, methodName, params);
        
        // Try block - wraps the actual method execution to catch exceptions
        try {
            // Execute the actual method - proceeds with the intercepted method call
            // This is where the real service method logic runs
            Object result = joinPoint.proceed();
            
            // Calculate execution time by subtracting start time from current time
            long executionTime = System.currentTimeMillis() - startTime;
            // Format the return value into a readable string - handles nulls, collections, and long values
            String returnValue = formatReturnValue(result);
            // Log method exit at DEBUG level with class name, method name, return value, and execution time in milliseconds
            // ← symbol indicates method exit/return
            log.debug("← {}.{} returned: {} [{}ms]", className, methodName, returnValue, executionTime);
            
            // Return the result from the actual method execution back to the caller
            return result;
            
        // Catch block - handles any exceptions thrown by the intercepted method
        } catch (Exception ex) {
            // Calculate execution time even when method fails - helps identify slow failing operations
            long executionTime = System.currentTimeMillis() - startTime;
            // Log method failure at ERROR level with class name, method name, and execution time
            // ✗ symbol indicates method failure
            log.error("✗ {}.{} failed after {}ms", className, methodName, executionTime);
            // Re-throw the exception to maintain normal exception propagation to the caller
            throw ex;
        }
    }


    // @AfterThrowing annotation - defines advice that executes when a matched method throws an exception
    // pointcut parameter specifies which methods to intercept (reuses serviceMethods() pointcut)
    // throwing parameter binds the thrown exception to the "ex" parameter
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    // Method that logs exceptions thrown by service methods - provides additional exception context
    // Takes the exception as a parameter for detailed logging
    public void logException(Exception ex) {
        // Log exception at ERROR level with exception type, message, and full stack trace
        // This complements the error log in the @Around advice by adding exception details
        log.error("Exception in service layer - type: {}, message: {}", 
                ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }


    // Private helper method to format method parameters into a readable string for logging
    // Takes array of parameter names and array of parameter values (must be same length)
    // Returns formatted string like "username=john, password=***MASKED***, id=123"
    private String formatParameters(String[] paramNames, Object[] paramValues) {
        // Check if parameters are null or empty - return empty string if no parameters to format
        if (paramNames == null || paramValues == null || paramNames.length == 0) {
            // Return empty string indicating no parameters present
            return "";
        }
        
        // Create StringBuilder for efficient string concatenation in loop
        StringBuilder sb = new StringBuilder();
        // Loop through each parameter by index - paramNames and paramValues have matching indices
        for (int i = 0; i < paramNames.length; i++) {
            // Check if this is not the first parameter - need to add comma separator
            if (i > 0) {
                // Append comma and space to separate parameters
                sb.append(", ");
            }
            
            // Get the name of the current parameter (e.g., "username", "password")
            String paramName = paramNames[i];
            // Get the value of the current parameter (e.g., "john", "secret123")
            // Get the value of the current parameter (e.g., "john", "secret123")
            Object paramValue = paramValues[i];
            
            // Append parameter name followed by equals sign (e.g., "username=")
            sb.append(paramName).append("=");
            
            // Handle null values - check if parameter value is null to avoid NullPointerException
            if (paramValue == null) {
                // Append the string "null" to indicate parameter was not provided
                sb.append("null");
                // Skip to next parameter using continue statement
                continue;
            }
            
            // Mask sensitive parameters - check if parameter name matches sensitive patterns (password, secret, etc.)
            if (isSensitiveParameter(paramName)) {
                // Append masked value instead of actual value for security
                sb.append("***MASKED***");
                // Skip to next parameter using continue statement
                continue;
            }
            
            // Special handling for MultipartFile - uploaded files need custom formatting
            if (paramValue instanceof MultipartFile file) {
                // Format file as "MultipartFile{name='filename.jpg', size=1024 bytes}" - shows file info without content
                sb.append(String.format("MultipartFile{name='%s', size=%d bytes}", 
                        file.getOriginalFilename(), file.getSize()));
                // Skip to next parameter using continue statement
                continue;
            }
            
            // Format regular parameters - convert parameter value to string for logging
            String valueStr = String.valueOf(paramValue);
            // Check if string is too long (>100 chars) to avoid cluttering logs with large objects
            if (valueStr.length() > 100) {
                // Truncate string to first 97 characters and add "..." to indicate truncation
                sb.append(valueStr, 0, 97).append("...");
            } else {
                // Append full value if it's short enough
                sb.append(valueStr);
            }
        }
        
        // Return the complete formatted parameter string
        return sb.toString();
    }


    private String formatReturnValue(Object returnValue) {
        if (returnValue == null) {
            return "null";
        }
        
        // Check if return value is an Optional using instanceof
        if (returnValue instanceof java.util.Optional<?> optional) {
            // Use ternary operator to check if Optional contains a value
            return optional.isPresent() ? 
                    // If present, recursively format the wrapped value and wrap in "Optional[...]"
                    "Optional[" + formatReturnValue(optional.get()) + "]" : 
                    // If empty, return "Optional.empty" to indicate no value
                    "Optional.empty";
        }
        
        // Check if return value implements Collection interface
        if (returnValue instanceof java.util.Collection<?> collection) {
            // Return collection type name and size, e.g., "ArrayList[size=5]"
            return collection.getClass().getSimpleName() + "[size=" + collection.size() + "]";
        }
        
        // Check if class name contains "Page" string (works for Spring Data Page implementations)
        if (returnValue.getClass().getName().contains("Page")) {
            // Try block - reflection might fail if Page interface changes
            try {
                // Use reflection to get getTotalElements() method from Page interface
                var pageMethod = returnValue.getClass().getMethod("getTotalElements");
                // Invoke the method to get total number of elements across all pages
                var totalElements = pageMethod.invoke(returnValue);
                // Return formatted string showing total elements, e.g., "Page[totalElements=100]"
                return "Page[totalElements=" + totalElements + "]";
            // Catch any reflection exceptions (method not found, invocation failed, etc.)
            } catch (Exception e) {
                // Return generic Page indicator if reflection fails
                return "Page[...]";
            }
        }
        
        // Truncate long strings - convert return value to string and check length
        String valueStr = String.valueOf(returnValue);
        // Check if string representation is too long (>200 chars) to avoid log bloat
        if (valueStr.length() > 200) {
            // Return class name with truncation indicator instead of full string
            return returnValue.getClass().getSimpleName() + "[truncated]";
        }
        
        // Return full string representation if it's short enough for logs
        return valueStr;
    }

    // Takes parameter name as string and returns true if it should be masked
    // Returns boolean indicating whether the parameter is sensitive
    private boolean isSensitiveParameter(String paramName) {
        // Check if parameter name is null - null names cannot be sensitive
        if (paramName == null) {
            // Return false indicating this is not a sensitive parameter
            return false;
        }
        
        // Convert parameter name to lowercase for case-insensitive matching ("Password" matches "password")
        String lowerName = paramName.toLowerCase();
        // Stream through the SENSITIVE_PATTERNS list (password, secret, token, etc.)
        return SENSITIVE_PATTERNS.stream()
                // Check if any pattern is contained in the parameter name using anyMatch
                // e.g., "userPassword" contains "password", "apiToken" contains "token"
                .anyMatch(lowerName::contains);
    }
}
