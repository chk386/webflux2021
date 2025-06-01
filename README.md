
# 211117 Spring5 Reactive Webflux
## 사전준비
- 필수 : JDK 17
- 필수 : GRADLE 7.2+
- 필수 : IDEA
- 선택 : [docker](https://docs.docker.com/desktop/mac/install/)
- 선택 : docker-compose 설치 확인

## 실행
```bash
git clone https://github.com/chk386/webflux2021
cd webflux2021
gradle clean assemble
gradle bootRun -Dspring.profiles.active=cloud
```
## 로컬에서 kafka, mysql, mongodb, redis 띄우고 실행하는 법
1. /docker/docker-compose up
2. 몽고 - db, 계정 생성
    ```bash
    docker exec -it mongo /bin/sh
    mongo -u nhn -p nhn
    use webflux
    db.createUser({user: "webflux", pwd: "webflux", roles: ["readWrite"]})
    ```
3. mysql 스키마 생성
   1. r2dbc:mysql://localhost:3306/webflux (webflux/webflux) 접속
   2. [schema-mysql.sql](/src/main/resources/schema-mysql.sql) 실행
4. gradle clean build
5. gradle bootRun

## image로 실행 (설정하기 싫다면..)
```bash
docker run -e spring.profiles.active=cloud -p 8080:8080 -m=1G chk386/webflux
```
## dockerizing 참고
```bash
gradle bootBuildImage --imageName=chk386/webflux:latest # container 만들기

# image upload to docker hub
docker login # docker hub 계정 필요
docker tag chk386/webflux:latest chk386/webflux:latest # docker hub repository로 변경필요
docker push chk386/webflux:latest
docker run -e spring.profiles.active=local -p 8080:8080 -m=2G chk386/webflux # 실행
```

## Spring Webflux?
[Web on Reactive Stack](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#spring-webflux
![webonreactrive](https://github.com/user-attachments/assets/5d5f429e-2841-43f9-8acc-6ba45ce73a78)




* Spring 5.x+
* fully non-blocking, back pressure
* Reactor Netty, undertow, Servlet 3.1+ Containers (tomcat 9.x)

### Why was Spring WebFlux created?/
* Part of the answer is the need for a non-blocking web stack to handle concurrency with a small number of threads and scale with fewer hardware resources.
* The other part of the answer is functional programming

## Programming models
<img width="846" alt="webflux_ven" src="https://github.com/user-attachments/assets/4f3acc60-4c2e-4006-a520-990cb0571333" />

``` java
// Annotated Controllers
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public Mono<String> processSubmit(@Valid @ModelAttribute("pet") Mono<Pet> petMono) {
    return petMono
        .flatMap(pet -> {
            // ...
        })
        .onErrorResume(ex -> {
            // ...
        });
}
```

# multi-thread vs event loop

## multi-thread

![blocking](https://howtodoinjava.com/wp-content/uploads/2019/02/Blocking-request-processing.png)
[출처](https://howtodoinjava.com/wp-content/uploads/2019/02/Blocking-request-processing.png)

Thread Pool

* blocking가 길어질경우 cpu, 메모리등 하드웨어 리소스는 여유롭지만 쓰레드 부족
* thread pool의 수를 늘리면 context switching비용 증가


## event loop
![](https://i.imgur.com/6zecDum.jpeg)
[출처](https://www.baeldung.com/spring-webflux-concurrency)

* event loop은 few threads로 동작한다.
* event loop은 순차적으로 event queue의 event를 처리하며 platform에 callback을 등록후 즉시 리턴한다.
* event loop은 작업이 완료된 callback을 triggering한다.
* 메인쓰레드는 small thread로 실행되며 내부,외부 IO(파일입출력, DB호출, http 통신등)에게 이벤트를 보내고 콜백(결과가 아니라 함수)을 전달하여 별도 쓰레드에서 작업 완료시 메인쓰레드에서 콜백이 실행
* 콜백이 오기 전까지 기다리지 않고 메인쓰레드에서는 다른 작업을 실행

## sync - async, blocking - nonblocking
![](https://i.imgur.com/oOJORZM.png)


* blocking: 실행이 끝나고 리턴
* nonblocking: 바로리턴
* sync : 실행 쓰레드에 콜백을 전달하지 않음. (완료여부를 알수 없다)
* async: 콜백을 넘겨주어서 완료시 메인쓰레드에서 콜백을 실행. (완료여부를 알수 있다.)

### Sync-Blocking
예 : servlet stack
### Sync-NonBlocking

``` java
// jdk 1.5
Future<Integer> future = new SquareCalculator().calculate(10);

while(!future.isDone()) {
    System.out.println("Calculating...");
    Thread.sleep(300);
}

Integer result = future.get();
```
### Async-Blocking
- spring webflux에서 jdbc를 사용할 경우
- node.js + mysql
### Async-NonBlocking
``` java
// jdk 1.8
CompletableFuture
                .supplyAsync(() -> "Hello, World")
                .exceptionally(Throwable::getMessage)
                .thenApply(s -> s + "!!!")
                .handle((s, t) -> s != null ? s : "Hello, Stranger!!!");
```
## JDK, Spring 버전별 Async 키워드
### jdk 1.5
Future
FutureTask
Callable
### jdk 8
CompletableFuture -> Mono.fromFuture(CompletableFuture::new)
### jdk9 - Flow API
Publisher
Subscriber
Subscription
Processor
### spring4
@Async
ListenableFuture
AsyncRestTemplate
deferredResult
WebAsyncTask
CompletionStage
ResponseBodyEmitter
### spring5 - 실습
Mono : https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/MonoTest.java
Flux : https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/FluxTest.java
## Reactive
* reactive : 변화에 반응 하는것, 반응형 프로그래밍
* network components reacting to I/O events
* UI controllers reacting to mouse
* non-blocking with backpressure
## Reactive Programming

> 인프라 스트럭처에 대한 도전 -> async-nonblocking
> 프로그래밍 모델의 전환 (보이지 않는 리소스 문제를 해결하기 위해 보이는 코드의 변화) -> functional programming

publisher가 subscriber를 압도하지 않도록 backpressure와 small thread를 사용하여 nonblocking/event driven 방식의 프로그래밍
## Reactive Streams

[공식 명세](https://www.reactive-streams.org/)

> Reactive Streams is a standard for asynchronous data processing in a streaming fashion with non-blocking back pressure.

* 2013년 말 neflix, pivotal, lightbend 엔지니어들이 시작
* 비동기 스트림 처리를 위해서 표준을 제공하기 위한 이니셔티브
* contributors : Lightbend(play, akka team), netflix, pivotal, Red Hat, Oracle, Twitter, [spray.io](http://spray.io)
* 구현체들
    * ReactiveX(cross-platform, Microsoft, Netflix)
        * [RxJava2Adapter](https://projectreactor.io/docs/adapter/release/api/reactor/adapter/rxjava/RxJava2Adapter.html)
    * ProjectReactor(Spring, Pivotal)


![](https://i.imgur.com/XI06khy.png)
[출처](https://engineering.linecorp.com/ko/reactivestreams1-1/)
### Interface
https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/ReactiveStreamTest.java

``` java
@FunctionalInterface
public static interface Publisher<t> {
	public void subscribe(Subscriber<? super T> subscriber);
}

public static interface Subscriber<T> {
	public void onSubscribe(Subscription subscription);
	public void onNext(T item);
	public void onError(Throwable throwable);
	public void onComplete();
}

public static interface Subscription {
	public void request(long n);
	public void cancel();
}

public static interface Processor<T,R> extends Subscriber<T>, Publisher<R> {
}
```

![](https://i.imgur.com/HuvnRKY.png)

[출처](https://ozenero.com/java-9-flow-api-reactive-streams)

### BackPressure
https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/BackPressureTest.java

subscriber는 publisher가 push해주는 데이터나 이벤트들의 흐름을 제어할 수 있도록 backpressure를 제공한다.

![backpressure](https://static.packt-cdn.com/products/9781789135794/graphics/d34656f2-5a74-4d27-9dd6-7dde5a4153c8.png)

### ProjectReactor

[https://projectreactor.io/](https://projectreactor.io/)

* reactive streams 인터페이스의 jvm 구현체
* pivotal에서 오픈소스로 관리
* Publisher의 구현체이며 수많은 operator를 제공
* subscriber와 publisher의 실행되는 쓰레드 풀을 지정하여 비동기 논블럭킹 프로그래밍을 쉽게 구현 가능
* 모든 마이크로 리엑티브 툴킷
    * spring boot and webflux, reactive client(redis, mongo, kafka, RSocket, R2DBC, Netty)

![스크린샷 2019-11-28 오전 12.22.45.png](/files/3139077616077350192)

[release note](https://github.com/reactor/reactor-core/releases?page=1)

Mono, Flux

* Flux : 0...N개의 데이터를 발행(emit), 하나의 데이터를 전달할때마다 onNext이벤트 발생, 모든 데이터가 푸시되면 oncomplete 이벤트 발생, 데이터를 전달하는 과정에서 오류가 발생하면 onError이벤트 발생
![flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/flux.svg)
* Mono : 0...1을 의미
![mono](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/mono.svg)

**Mono.subscribe(), Flux.subscribe()가 실행되는 순간 publisher는 이벤트(데이터)를 emit한다.**
<br>
### Iterable vs Observable vs Reactive Streams

| Iterable | Observable | Reactive Streams |
| --- | --- | --- |
| it.next() | notifyObservers(i) | s.onNext(i) |
| E next(); | void notifyObservers(Object arg) | void onNext(T t) |
| pull | push | push |

전통적인 비동기 프로그래밍 방식인 Observable과 비교하여 다음과 같은 장점이 있다.

* 에러전파 구현이 쉽다.
* 완료 시점을 알수 있다.
* backpressure

## Spring5 Webflux 실습

* [ReactiveStreamTest.java](https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/ReactiveStreamTest.java)
    * Iterable Pattern
    * Observer Pattern
    * Reactive Stream + BackPressure
    * Reactor
* [MonoTest.java](https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/MonoTest.java)
    * Mono생성 & 테스트
    * map : 동기, 값을 변환 , flatMap : 비동기, 새로운 publisher 변환
    * 쓰레드 격리
    * error발생
    * emit된 mono -> flux변환, delay
    * Publisher 테스트를 위한 StepVerifier
    * Mono.fisrt, Mono.zip, mono.zipWith
    * 비동기 세상에서는 가장 긴 실행시간이 전체 실행시간
* [FluxTest.java](https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/FluxTest.java)
    * Flux생성 & 테스트
    * Flux.range , Flux.inteval
    * Flux.buffer : emit된 데이터를 n개까지 모아서 반환
    * StepVerifier.recordWith
    * log에서 cancel이유는? take
    * publishOn vs subscribeOn
    * Flux.groupBy, MonoCount
    * Flux.merge(비동기) vs Flux.concat(동기)
* [BackPressureTest.java](https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/BackPressureTest.java)
    * BaseSubscriber.hookOnNext
* [HotPublisherTest.java](https://github.com/chk386/webflux2021/blob/main/src/test/java/com/nhn/webflux2021/HotPublisherTest.java)
    * cold vs hot
    * [Sinks](https://projectreactor.io/docs/core/release/reference/#sinks)
    * Sinks.many().multicast().directBestEffort() vs onBackpressureBuffer() : [reference](https://projectreactor.io/docs/core/3.4.0/api/reactor/core/publisher/Sinks.MulticastSpec.html)
* webflux2021 spring diagram
![](https://i.imgur.com/uekLqyD.png)

* WebfluxConfig
    * @EnableWebflux
    * WebFluxConfigurer
    * AbstractErrorWebExceptionHandler : 글로벌 exception handler, @ExceptionHandler(Exception.class) 금지
* MemberRouter
    * `RouterFunction`
    * route(), before, path, nest,
    * before, after
    * `HandlerFunction`
* MemberHandler : ServerRequest, ServerResponse, request.bodyToMono, BodyExtractoers(multipart), WebClient(no AsyncRestTemplate)
    * /http/webflux.http 순서대로 실행
    * [blocking call](https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking)
* MemberRouterTest
    * @WebFluxTest
    * WebTestClient (no MockMvc)
* MemberHistoryReactiveRepositoryTest
    * @EnableReactiveMongoRepositories
    * @DataMongoTest
* MemberReactiveRepositoryTest
    * @EnableR2dbcRepositories
    * @DataR2dbcTest
    * ReactiveCrudRepository
    * R2dbcEntityTemplate
    * TransactionalOperator (선언적 트랜잭션 X, programatic) : commit, rollback
* MemberCacheTest
    * ReactiveRedisTemplate
    * @RedisHash X, RedisRepository X
* WebSocketConfig
    * /websocket.html 접속
    * @Bean multicast()등록, hot publisher
    * input과 output을 모두 구현
    * bufferTimout : n개가 emited될때까지 또는 n초까지 모아서 처리
    * ReactiveMongoTemplate, @Document
    * [reference](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-websocket)
    * 서버가 N대 일떄? [redis pub-sub](https://github.com/chk386/spring5-reactive-webflux/blob/master/src/main/java/com/nhnent/webfluxtest/user/UserRedisHandler.java)
* KafkaConfig
    * KafkaProperties <- application.yml
    * ReactiveKafkaProducerTemplate
    * key in -> map -> kafka produce -> @Bean multicast()

## event(data) stream

![the full graph of a flow ](https://raw.githubusercontent.com/reactor/projectreactor.io/27f2d8e3cbbed8f8e0d8d27f9c423c906adbbaa4/src/main/static/assets/img/reactor.gif)
## 결론
* webflux가 빠르다? No!
* 외부 I/O 응답이 느릴수록 동시에 호출하는 것이 많을수록 Non-Blocking의 장점이 극대화 된다.
* functional programming : jdk8 lambda, functional interface, higher order function, Non-Iterable, method chaining
* `Declarative` vs Imperative
* (개인 의견) kotlin으로... 코드 살펴보기(coRouter, coroutine, transaction)
    * [blocking transaction](https://github.nhnent.com/ncp/member/blob/master/shop/src/main/kotlin/com/ncp/member/application/member/MemberCommandService.kt)
    * [Thread Isolation](https://github.nhnent.com/ncp/product/blob/master/admin/src/main/kotlin/com/ncp/product/configuration/ThreadPoolComponent.kt)
    * [coRouter](https://github.nhnent.com/ncp/member/blob/master/shop/src/main/kotlin/com/ncp/member/presentation/router/ProfileRouter.kt)
* jpa와 같은 jdbc를 사용할 경우(특히 insert, update, delete) 실행되는 thread를 반드시 확인할것.

## 더 공부해볼만한 것들

* [webflux functional endpoint 문서화](https://youtu.be/qguXHW0s8RY?t=507)
* [reactive kafka consumer 참고](https://github.com/chk386/webflux-example/blob/master/reactive/src/main/java/com/nhn/webflux/configuration/KafkaConfiguration.java)
* [redis pub/sub 구현 참고](https://github.com/chk386/spring5-reactive-webflux/blob/master/src/main/java/com/nhnent/webfluxtest/user/UserRedisHandler.java)
* [flux를 모아서 중복없이 처리하는 방법](https://www.youtube.com/watch?v=HzQfJNusnO8&t=1166s&ab_channel=NHNCloud) : Flux.bufferTimeout(), Flux.groupBy(), Flux.create(sink)


