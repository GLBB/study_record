# ArrayList 成员变量

一直以来对 ArrayList 的理解是内部包含一个数组，封装了对数组的一些操作。现在秋招，复习基础，就将集合框架翻出来看看。

## 成员变量

首先先看存储元素的数组：

```
transient Object[] elementData;
```

对这个成员变量可以提出一些疑问：

Q1: 为什么不是 E[] elementData？

A1: 范型无法实例化

Q2: 为什么是 transient, 序列化不保存元素吗？

A2：ObjectOutputStream 在序列化时会通过 writeObjet() -> writeObjet0() -> writeSerialData() -> 反射调用 ArrayList 中的 writeObjet 方法，所以元素也会保存。

Q3: 这样做的原因，直接使用默认的序列化不好吗？

A3: ArrayList 为了避免频繁扩容，通常会保留很多空的元素。ObjectOutputStream  对数组的保存策略遍历所有数组元素，如果数组元素为null, 会写 null。而 ArrayList 保存元素是只遍历前面包含数据，自定义序列化带来的好处减少遍历次数，同时不会写 null，减少序列化时间和空间。

### 其他成员变量

```
private static final long serialVersionUID = 8683452581122892189L;
// 如果 new 的时候没有指定，默认数组长度是 10
private static final int DEFAULT_CAPACITY = 10;
// 数组中有效数据个数
private int size;
```

下面两个成员变量：

```
private static final Object[] EMPTY_ELEMENTDATA = {};
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
```

这里 EMPTY_ELEMENTDATA 注意是 static的，数组是懒加载生成的，避免客户端创建大量空的 ArrayList。

无参构造方法，使用的是 DEFAULTCAPACITY_EMPTY_ELEMENTDATA， 其他构造方法使用的都是 EMPTY_ELEMENTDATA。

扩容时会进行判断，如果初始化是 DEFAULTCAPACITY_EMPTY_ELEMENTDATA，那么扩容时以数组长度为10开始，EMPTY_ELEMENTDATA 以数组长度1开始扩容，都是扩容1.5倍。

即无参构造函数扩容得更快。

传入集合或容量为0的构造参数扩容得更慢。

## 扩容

add 元素时会对容量进行判断，如果数组满了，会扩容1.5倍。







