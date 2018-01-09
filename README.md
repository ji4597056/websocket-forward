# Websocket Forward

- The simple library is used to support the forward proxy of websocket for applications which are based in the framework of `spring boot`.
- The library is based on `JDK8`.

## Usage

`spring-websocket-forward` is available from `Maven Central`.

```java
	<dependency>
		<groupId>com.github.ji4597056</groupId>
		<artifactId>spring-websocket-forward</artifactId>
		<version>1.0.2.RELEASE</version>
	</dependency>
```

## Who is this for?

Spring Cloud Zuul and Spring Boot Websocket do not support the forwarding proxy of web sockets. However, we usually need the proxy of weboscket.

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
With this you can accurately get websockets support from proxied back-end service.

## Configuration introduction

| property  | type | required | default | introduction |
|--------|--------|--------|--------|-------|
| enabled            | boolean  | false | true   | enabled or not |
| prefix             | string   | false | null   | uri route prefix |
| uri                | string   | true  | null   | uri |
| withJs             | boolean  | false | false  | with JS or not |
| forwardPrefix      | string   | false | null   | forwarding uri route prefix |
| serviceId          | string   | false | null   | find forwarding addresses from registration center by `service id` |
| listOfServices     | string[] | false | null   | find forwarding addresses from list of services if `serviceId` is not set,eg:localhost:8080 |
| allowedOrigins     | string[] | false | "*"    | allowed origins |
| handlerClass       | string   | false | null   | name of `AbstractWsServerHandler` class,use global handler if there is not set |
| interceptorClasses | string[] | false | null   | name of `HandshakeInterceptor` class,use global interceptors if there is not set |

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
Then you can set the property of `handlerClass` or `interceptorClasses`.
