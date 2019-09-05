# ConcurrentHashMap 解析

## 方法

```java
/**
     * Returns a power of two table size for the given desired capacity.
     * See Hackers Delight, sec 3.2
     */
private static final int tableSizeFor(int c) {
    int n = -1 >>> Integer.numberOfLeadingZeros(c - 1);
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

将 c 扩充到 2的 n 次方

如 c = 10, 返回值是 16， 可以通过笔算一下。

