# Eos Ecosystem
 
**99** Performance rating on Google Chrome's Lighthouse.

EOS is an Open Source Java Web Server.
An alternative to Tomcat, Jetty & Wildfly with built in 
dependency injection, request routing and data management.

Say what? Finally, an alternative. Whats great about Eos 
is that you no longer have to set up a seperate application 
server and deploy .war files. Thats right, no more .WAR.
Instead you run the application from its root directory with a 
simple command.

Want to test drive? Run the following command from the 
root eos/ directory:

```
$ ./gradlew eos-examples:simple-rest-endpoint:run
```

browse:

```
http://localhost:8080/
```

 
Also, if you are tired of deploying war files, 
EOS projects can run as exploded jars or as a single fat uber jar!

### Running the Server
```
public static void main(String[] arguments){
    EOS eos = new EOS.Builder().withPort(3000).spawn(20).make();
    eos.start();
}
```

### What is a eos.fragment?


### Built-in view tag library
The tag library is really a work in progress! Just the bare minimum 
right now. One level down look ups on objects. One level down for each
iterable. Here is what they look like: 

```
<a:if condition="${state.id == town.stateId}"></a:if>
<a:each in="${states}" item="state"></a:each>
<a:set variable="selected" value="selected"/>
```

### todos 

# | subject | condition | predicate | complete
| --- | --- | --- | --- | ---
1 | resp.object | == | null |
2 | resp.object | != | '' |
3 | resp.object | == | '' |
4 | resp.object | != | 'value' |
5 | resp.object | == | 'value' |
6 | resp.object.object | == | null |
7 | resp.object.object | != | '' |
8 | resp.object.object | == | '' |
9 | resp.object.object | != | 'value' |
10 | resp.object.object | == | 'value' |
11 | resp.object.object.object | == | null |
12 | resp.object.object.object | != | '' |
13 | resp.object.object.object | == | '' |
14 | resp.object.object.object | != | 'value' |
15 | resp.object.object.object | == | 'value' |
16 | iterable.object | == | null |
17 | iterable.object | != | '' |
18 | iterable.object | == | '' |
19 | iterable.object | != | 'value' |
20 | iterable.object | == | 'value' |
21 | iterable.object.object | == | null |
22 | iterable.object.object | != | '' |
23 | iterable.object.object | == | '' |
24 | iterable.object.object | != | 'value' |
25 | iterable.object.object | == | 'value' |
26 | iterable.object.object.object | == | null |
27 | iterable.object.object.object | != | '' |
28 | iterable.object.object.object | == | '' |
29 | iterable.object.object.object | != | 'value' |
30 | iterable.object.object.object | == | 'value' |












