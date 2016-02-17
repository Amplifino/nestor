# com.amplifino.nestor.soap

Whiteboard implementation for soap web services using the spi feature of JAX-WS 2.2.
Tested with the JAX-WS Reference Implementation included in Java 8 SE.
Should work with other JAX-WS 2.2 implementations by publishing a Provider service.

Any service with a com.amplifino.soap.endpoint property will be published,
with endpoint http://hostname:portnumber/soap/propertyValue. 

The value of the prefix (/soap) can be overruled in Config Admin.

By default the whiteboard uses Java 8's builtin JAX-WS support, 
but this can be overruled by registering an alternative javax.xml.ws.spi.Provider as OSGI service.  
 



