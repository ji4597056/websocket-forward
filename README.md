# Websocket Forward

- A simple library to enable web socket proxy support in spring boot applications.

## USAGE

`spring-websocket-forward` is available from `Maven Central`.

```java
	<dependency>
		<groupId>com.github.ji4597056</groupId>
		<artifactId>spring-websocket-forward</artifactId>
		<version>1.0.0.RELEASE</version>
	</dependency>
```

## Who is this for?

Spring Cloud Zuul and Spring Boot Websocket does not natively support web sockets to proxy requests.Usually,we need to use the weboscket proxy.

## How do I use this?

- Enable it like so:

```java
@EnableWsForward
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

- Then in your spring application properties(e.g `application.yml`)

```yml
ws:
  forward:
    enabled: true
    handlers:
      test:
        enabled: true
        prefix: /test
        uri: /**
        serviceId: test-client
        allowedOrigins: "*"
        withJs: false
```
With this you should have web sockets to your back-end service working correctly.

## Configuration instruction

| property  | type | required | default | introduction |
|--------|--------|--------|--------|-------|
| enabled            | boolean  | false | true   | is enabled |  
| prefix             | string   | false | null   | uri route prefix |
| uri                | string   | true  | null   | uri |
| withJs             | boolean  | false | false  | is withJs |
| forwardPrefix      | string   | false | null   | forward prefix |    
| serviceId          | string   | false | null   | find forward addresses from registration center by service id |
| listOfServices     | string[] | false | null   | find forward addresses from list of services if not setting `serviceId`,eg:localhost:8080 |
| allowedOrigins     | string[] | false | null   | allowed origins |
| handlerClass       | string   | false | null   | name of `AbstractWsServerHandler` class,use global handler if not setting |
| interceptorClasses | string[] | false | null   | name of `HandshakeInterceptor` class,use global interceptors if not setting |

## Remark

if you want to set your own `AbstractWsServerHandler` or `HandshakeInterceptor`,config bean of `WsHandlerRegistration` like:
```java
    @Bean
    public WsHandlerRegistration wsHandlerRegistration() {
        WsHandlerRegistration registration = new DefaultWsHandlerRegistration();
        registration.addHandler(new TestWsServerHandler());
        registration.addInterceptor(new TestHandshakeInterceptor());
        return registration;
    }
```
Then you can set the property of handlerClass` or `interceptorClasses`.