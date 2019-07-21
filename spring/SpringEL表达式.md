# SpEL 表达式

> SpEL 表达式可以使用在 spring 的所有产品中。

## 使用示例

**示例一**

```
ExpressionParser parser = new SpelExpressionParser();
Expression expr = parser.parseExpression("'Hello World'");
String msg = (String) expr.getValue();
System.out.println(msg);
```

msg 是 Hello World

