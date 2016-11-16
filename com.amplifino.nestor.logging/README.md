# com.amplifino.nestor.logging #

Bundle to forward log messages from java.util.logging to OSGI Log Service.
Requires Declarative Services.  
At component activation the bridge installs a log handler on java.util.logging's root Logger with a default log level of INFO.  
Default Log Level can be changed by an optional Configuration Admin config or using the LogBridge service. 
Note that the bundle only configures the handler's log level, the basic java.util.logging configuration is unchanged.
Java.util.Loggers have their own log level with a default of INFO.

To see Levels below INFO in the Webconsole Log Service View is rather involved:

- Start your OSGI container with -Dorg.apache.felix.log.storeDebug=true
- In the log bridge configuration change the forwarding level to a level below INFO, e.g. FINE.
- Programmatically change the log level of Loggers of interest to FINE

## Version history: ##

- 1.0.0: Initial version


