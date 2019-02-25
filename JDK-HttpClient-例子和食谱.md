# JDK-HttpClient 例子和食谱

这篇文章有很多关于 HttpClient 用于日常任务例子。这是HttpClient 的介绍[HttpClient 介绍](./JDK-HttpClient-介绍.md) 

* 同步 Get
* 异步 Get
* Post
* 并发请求
* Get JSON
* Post JSON
* 设置代理

## 同步Get

响应体作为一个String

```java
public void get(String uri) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(uri))
          .build();

    HttpResponse<String> response =
          client.send(request, BodyHandlers.ofString());

    System.out.println(response.body());
}
```

上面的例子使用了 BodyHandlers#ofString() 来把响应体字节转换成字符串，BodyHandlers 必须用于每一个 HttpRequest 来决定如何处理响应体（如果有响应体的话）。

当响应码和响应头可以得到的时候，但在响应体到达之前，BodyHandlers 被调用一次。`BodyHandler`  负责创建 `BodySubscriber`  ，`BodySubscriber`  是响应式流（reactive-stream）的订阅者，非阻塞式的接受数据流，负责将响应体字节转化为更高等级的 Java 中的类型。

这个类 `HttpResponse.BodyHandlers` 提供了很多方便的静态工厂方法来创建 `BodyHandler `, 他们接收响应体字节放在内存中，直到完全收到响应体的字节，然后就把这些收到的字节转化为更高等级的 Java 类型，例如，`ofString ` 和 `ofByteArray `。响应数据到达后作为流也是可以的；`ofFile `，`ofByteArrayConsumer `和`ofInputStream `。另外，还可以自定义订阅者。

相应体数据作为文件

```java
public void get(String uri) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(uri))
          .build();

    HttpResponse<Path> response =
          client.send(request, BodyHandlers.ofFile(Paths.get("body.txt")));

    System.out.println("Response in file:" + response.body());
}
```

## 异步Get

异步API马上返回`CompletableFuture`   来处理` HttpResponse` 。在 Java 8 的时候，加入了 `CompletableFuture`  用于支持可组合的异步编程。

响应体作为字符串

```java
public CompletableFuture<String> get(String uri) {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(uri))
          .build();

    return client.sendAsync(request, BodyHandlers.ofString())
          .thenApply(HttpResponse::body);
}
```

 `CompletableFuture.thenApply(Function)` 方法用于把 `HttpResponse`  映射成为 `body` 的类型，状态码等等。

响应体作为一个文件：

```java
public CompletableFuture<Path> get(String uri) {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(uri))
          .build();

    return client.sendAsync(request, BodyHandlers.ofFile(Paths.get("body.txt")))
          .thenApply(HttpResponse::body);
}
```

## Post

`HttpRequest.BodyPublisher ` 支持请求体。

```java
public void post(String uri, String data) throws Exception {
    HttpClient client = HttpClient.newBuilder().build();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .POST(BodyPublishers.ofString(data))
            .build();

    HttpResponse<?> response = client.send(request, BodyHandlers.discarding());
    System.out.println(response.statusCode());
}
```

上面的例子用了 `BodyPublishers#ofString()`来把字符串转化为请求的响应体。

`BodyPublishers`是响应式流的发布者，在需要的时候，发布请求体的流。`HttpRequest.Builder `有很多方法允许设置一个`BodyPublisher `, `Builder::POST `,`Builder::PUT `和`Builder::method `。`HttpRequest.BodyPublishers `类有很多方便的静态工厂方法来创建 `BodyPublisher`  来处理常见的数据类型;`ofString`, `ofByteArray`, `ofFile` 。

`BodyHandlers#discarding()`用来接受响应体，和如果不感兴趣的话，就抛弃响应体。

## 并发请求

结合`java stream`和 `CompletableFuture  `API 可以非常容易的发送很多请求和等待这些请求的响应。下面的例子展示了对每一个列表中的 URI  发送GET 请求和存储这些请求的响应作为字符串。

```java
public void getURIs(List<URI> uris) {
    HttpClient client = HttpClient.newHttpClient();
    List<HttpRequest> requests = uris.stream()
            .map(HttpRequest::newBuilder)
            .map(reqBuilder -> reqBuilder.build())
            .collect(toList());

    CompletableFuture.allOf(requests.stream()
            .map(request -> client.sendAsync(request, ofString()))
            .toArray(CompletableFuture<?>[]::new))
            .join();
}
```

## Get JSON

如何响应体是 JSON 类型的话，可以很容易的利用第三方库转化响应体。

下面的例子展示了如何使用 Jackson 库，结合 `BodyHandlers::ofString `来把JSON 响应转化为 `Map`（key value 格式）。

```java
public CompletableFuture<Map<String,String>> JSONBodyAsMap(URI uri) {
    UncheckedObjectMapper objectMapper = new UncheckedObjectMapper();

    HttpRequest request = HttpRequest.newBuilder(uri)
          .header("Accept", "application/json")
          .build();

    return HttpClient.newHttpClient()
          .sendAsync(request, BodyHandlers.ofString())
          .thenApply(HttpResponse::body)
          .thenApply(objectMapper::readValue);
}

class UncheckedObjectMapper extends com.fasterxml.jackson.databind.ObjectMapper {
    /** Parses the given JSON string into a Map. */
    Map<String,String> readValue(String content) {
    try {
        return this.readValue(content, new TypeReference<>(){});
    } catch (IOException ioe) {
        throw new CompletionException(ioe);
    }
}
```

上面的例子使用 `ofString()`来积累内存中的响应体字节。另外，流的订阅者像`ofInputStream `也是可以使用的。

## Post JSON

在很多情况下，请求体可能是JSON，利用请求体处理者和第三方库很容易实现请求体是JSON格式。

下面的例子展示了如何使用 Jackson 库，结合这个 `BodyPublishers::ofString `转化 Map<String,String>类型为JSON

```java
public CompletableFuture<Void> postJSON(URI uri,
                                        Map<String,String> map)
    throws IOException
{
    ObjectMapper objectMapper = new ObjectMapper();
    String requestBody = objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(map);

    HttpRequest request = HttpRequest.newBuilder(uri)
          .header("Content-Type", "application/json")
          .POST(BodyPublishers.ofString(requestBody))
          .build();

    return HttpClient.newHttpClient()
          .sendAsync(request, BodyHandlers.ofString())
          .thenApply(HttpResponse::statusCode)
          .thenAccept(System.out::println);
}
```

## 设置代理

`ProxySelector`  可以用来配置 `HttpClient`  通过 client 的 `Builder::proxy `方法。`ProxySelector`  API 返回一个指定的代理对于指定的URI，在很多情况下，一个单独的静态代理是足够的。`ProxySelector::of `静态工厂方法用来创建 `selector`

使用代理，返回体转化为字符串

```java
public CompletableFuture<String> get(String uri) {
    HttpClient client = HttpClient.newBuilder()
          .proxy(ProxySelector.of(new InetSocketAddress("www-proxy.com", 8080)))
          .build();

    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(uri))
          .build();

    return client.sendAsync(request, BodyHandlers.ofString())
          .thenApply(HttpResponse::body);
}
```

另外，可以使用默认的系统代理。

```java
HttpClient.newBuilder()
      .proxy(ProxySelector.getDefault())
      .build();
```

