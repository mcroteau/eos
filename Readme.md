# A8i/

A8i is an Open Source Java Web Server.
An alternative to Tomcat & Jetty with built in 
dependency injection, request routing and data management. 
Also, if you are tired of deploying war files, 
A8i projects can run as exploded jars or as a single fat uber jar!

### Running the Server
```
public static void main(String[] arguments){
    A8i.Server server = new A8i.ServerBuilder().withPort(3001).spawn(150).make();
    server.registerPointcut(new GuestPointcut());
    server.registerPointcut(new AuthenticatedPointcut());
    server.registerPointcut(new UsernamePointcut());
    server.registerInterceptor(new GarciaInterceptor());
    server.run();
}
```

### What is a pointcut?


### Built-in view tag library
The tag library is really a work in progress! Just the bare minimum 
right now. One level down look ups on objects. One level down for each
iterable. Here is what they look like: 

```
<a:if condition="${state.id == town.stateId}"></a:if>
<a:each in="${states}" item="state"></a:each>
<a:set variable="selected" value="selected"/>
```

rhino
eros





