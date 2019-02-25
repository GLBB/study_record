# Java HTTP Client 介绍

Java 11 有了 HTTP Client, 可以用来通过网络请求 HTTP 资源。HTTP Client 支持 HTTP/1.1 和 HTTP/2, 同步和异步的编程模型，处理请求和响应，和响应体作为 [reactive-streams](http://www.reactive-streams.org/) ，和遵循了熟悉的构建模式。

**例子：GET 请求打印响应体作为字符串**

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("http://openjdk.java.net/"))
      .build();
client.sendAsync(request, BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenAccept(System.out::println)
      .join();
```

## HttpClient

想要发送请求，首先创建 HttpClient 通过它的构建者，构建者可以配置 HttpClient 的每一个状态，如：

* 首选协议版本（HTTP/1.1 或者 HTTP/2）
* 是否允许重定向
* 代理
* 认证

```
HttpClient client = HttpClient.newBuilder()
      .version(Version.HTTP_2)
      .followRedirects(Redirect.SAME_PROTOCOL)
      .proxy(ProxySelector.of(new InetSocketAddress("www-proxy.com", 8080)))
      .authenticator(Authenticator.getDefault())
      .build();
```

一旦建立，一个HttpClient可以发送多个请求。

## HttpRequest

创建HttpRequest通过它的构建者，这个请求的构建者可以设置：

* 请求的URI
* 请求方式（GET, POST, PUT）
* 请求体（如果有的话）
* 超时时间
* 请求头

```
HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("http://openjdk.java.net/"))
      .timeout(Duration.ofMinutes(1))
      .header("Content-Type", "application/json")
      .POST(BodyPublishers.ofFile(Paths.get("file.json")))
      .build()
```

一旦建立HttpRequest ， HttpRequest是不可变的，可以多次发送。

# 同步或者异步

发送请求可以同步或者异步，这个同步API，如你所望，会阻塞直到这个 HttpResponse 可用。

```java
HttpResponse<String> response =
      client.send(request, BodyHandlers.ofString());
System.out.println(response.statusCode());
System.out.println(response.body());
```

异步API立即返回对象*CompletableFuture*，当*HttpResponse*可用时，会利用*CompletableFuture*来处理*HttpResponse*。 *CompletableFuture*在Java8 时添加进入JDK，和支持组件式的异步编程。

```java
client.sendAsync(request, BodyHandlers.ofString())
      .thenApply(response -> { System.out.println(response.statusCode());
                               return response; } )
      .thenApply(HttpResponse::body)
      .thenAccept(System.out::println);
```

## 数据作为 reactive-streams

请求和响应体被当作 reactive-streams(异步流数据的非阻塞特性会减少压力)。事实上，HttpClient	是请求体的订阅者和响应体的发布者。BodyHandler  接口可以在真正的响应体到达之前，检查响应码和响应头，和承担着创建响应的BodySubscriber 

```java
public abstract class HttpRequest {
    ...
    public interface BodyPublisher
                extends Flow.Publisher<ByteBuffer> { ... }
}

public abstract class HttpResponse<T> {
    ...
    public interface BodyHandler<T> {
        BodySubscriber<T> apply(int statusCode, HttpHeaders responseHeaders);
    }

    public interface BodySubscriber<T>
                extends Flow.Subscriber<List<ByteBuffer>> { ... }
}
```

HttpRequest 和 HttpResponse 提供了很多方便的工厂方法来创造请求发布者和响应订阅者来处理通常的响应体，如文件，字符串，字节数组等。以文件来说，这些实现积累数据直到可以创建更高等级的Java类型，如 String, 或者 数据的stream。`BodySubscriber`  和 `BodyPublisher`  接口可以被实现来处理数据作为自定义的响应式流。

```java
HttpRequest.BodyPublishers::ofByteArray(byte[])
HttpRequest.BodyPublishers::ofByteArrays(Iterable)
HttpRequest.BodyPublishers::ofFile(Path)
HttpRequest.BodyPublishers::ofString(String)
HttpRequest.BodyPublishers::ofInputStream(Supplier<InputStream>)

HttpResponse.BodyHandlers::ofByteArray()
HttpResponse.BodyHandlers::ofString()
HttpResponse.BodyHandlers::ofFile(Path)
HttpResponse.BodyHandlers::discarding()
```

这些适配器位于 java.util.concurrent.Flow 的 `Publisher`/`Subscriber` 类型到 HTTP Client 的 `BodyPublisher`/`BodySubscriber` 类型。

```
HttpRequest.BodyPublishers::fromPublisher(...)

HttpResponse.BodyHandlers::fromSubscriber(...)
HttpResponse.BodyHandlers::fromLineSubscriber(...)
```

## HTTP/2

Java HttpClient 支持 HTTP/1.1 和 HTTP/2。默认的 httpclient 会发送请求使用 HTTP/2。请求发送到服务器，若服务器不支持，那么 HTTP/2会自动降级到HTTP/1.1。下面是一些简要的概述关于HTTP/2带来的提升：

* 头压缩。HTTP/2 使用 HPACK  压缩，来减少开销。
* 和服务器之间只有一个连接, 减少建立多个TCP连接需要的往返次数。
* 复用。多个请求可以同时，利用同样的连接。
* 服务器推送，未来附加的资源可以发送给客户端。
* 二进制格式，更加简洁。

因为HTTP/2是默认的推荐协议，和当必要时，实现可以无缝的退回HTTP/1.1。Java HttpClient 目标是在未来会大规模使用，当HTTP/2更广泛的部署时。

## 参考

- [The HTTP Client API documentation in Java 11](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html)

