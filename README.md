# stack-guard

stack-guard是一种尾调用优化技术。

[尾调用](https://baike.baidu.com/item/%E5%B0%BE%E8%B0%83%E7%94%A8?fromModule=lemma_search-box)是指一个函数的最后一条语句是对另一个函数的调用，后续没有其它操作。大多数程序都不是尾调用形式的，但是可以轻易地改写成尾调用形式。

[尾调用优化](https://www.ruanyifeng.com/blog/2015/04/tail-call.html)是指在函数调用另一个函数后没有执行其他操作时，编译器可以重用当前栈帧而不是创建新的栈帧，从而减少内存使用并提高程序执行效率。尾递归优化可以打破程序执行对系统调用栈大小的依赖，这对递归程序尤为重要。

所以，对于支持尾调用优化的编程语言来说，尾调用程序的执行是不受到系统栈大小的限制的。然而，并不是所有编程语言都支持尾调用优化，许多常用编程语言如Java、JavaScript等都没有实现尾调用优化。

stack-guard可以在任意支持异常的编程语言中实现尾调用优化，而无需依赖于编译器或解释器的尾调用优化，其原理如下：

1. 对于每个处于尾调用位置的函数，执行前检测当前递归深度，如果超过限制则抛出`ExitRecursionException`，并将当前函数的执行逻辑封装成`Runnable`随异常一起抛出
2. 最外层捕获到从多层递归中抛出的`ExitRecursionException`后，从中取出携带的`Runnable`并运行

## StackGuard

`StackGuard`工具类中包含了主要的工具方法：

| 方法                                          | 含义                                                                                       |
|---------------------------------------------|------------------------------------------------------------------------------------------|
| `void guard(Runnable runnable)`             | 检测当前递归深度，并在递归深度超过限制时抛出`ExitRecursionException`，`runnable`封装当前函数的执行逻辑，`runnable`将在跳出递归后执行 |
| `void run(Runnable runnable, int maxDepth)` | 运行被`guard`保护的函数，`maxDepth`为递归深度限制                                                        |

## Cont<T>

`Cont<T>`封装了continuation，与JDK自带的`Consumer<T>`类似，不同之处在于`accept`方法包含了`StackGuard.guard(...)`逻辑。

## 示例

下面是一些使用`StackGuard`的示例。

### 示例1：求和

```java
// 普通递归（非尾调用形式）
private long sum1(long n) {
    if (n == 1) {
        return 1;
    }
    return sum1(n - 1) + n;
}

// 改写成尾调用形式
private void sum2(long n, Consumer<Long> cont) {
    if (n == 1) {
        cont.accept(1L);
        return;
    }
    sum2(n - 1, r -> cont.accept(n + r));
}

// 使用StackGuard改写
private void sum3(long n, Cont<Long> cont) {
    guard(() -> sum3(n, cont));
    if (n == 1) {
        cont.accept(1L);
        return;
    }
    sum3(n - 1, r -> cont.accept(n + r));
}

System.out.println(sum1(1000000)); // StackOverflowError
sum2(1000000, System.out::println); // StackOverflowError
StackGuard.run(() -> sum3(1000000, System.out::println), 1000); // 500000500000
```

### 示例2：Fibonacci数列

```java
// 普通递归（非尾调用形式）
private int fib1(int n) {
    if (n == 1 || n == 2) {
        return n;
    }
    return fib1(n - 1) + fib1(n - 2);
}

// 改写成尾调用形式
private void fib2(int n, Consumer<Integer> cont) {
    if (n == 1 || n == 2) {
        cont.accept(n);
        return;
    }
    fib2(n - 1, a ->
        fib2(n - 2, b ->
            cont.accept(a + b)));
}

// 使用StackGuard改写
private void fib3(int n, Cont<Integer> cont) {
    guard(() -> fib3(n, cont));
    if (n == 1 || n == 2) {
        cont.accept(n);
        return;
    }
    fib3(n - 1, a ->
        fib3(n - 2, b ->
            cont.accept(a + b)));
}

System.out.println(fib1(1000000)); // StackOverflowError
fib2(1000000, System.out::println); // StackOverflowError
StackGuard.run(() -> fib3(1000000, System.out::println), 1000); // 没有栈溢出，执行很长时间
```

### 示例3：二叉树前序遍历

```java
// 普通递归
private void traverse1(TreeNode n, List<Integer> result) {
    if (n == null) {
        return;
    }

    result.add(n.val);
    traverse1(n.left, result);
    traverse1(n.right, result);
}

// 改写成尾调用形式
private void traverse2(TreeNode n, List<Integer> result, Runnable cont) {
    if (n == null) {
        cont.run();
        return;
    }

    result.add(n.val);
    traverse2(n.left, result, () ->
        traverse2(n.right, result, cont));
}

// 使用StackGuard改写
private void traverse3(TreeNode n, List<Integer> result, Cont<Void> cont) {
    guard(() -> traverse3(n, result, cont));
    if (n == null) {
        cont.accept(null);
        return;
    }

    result.add(n.val);
    traverse3(n.left, result, r ->
        traverse3(n.right, result, cont));
}

// 创建一棵很大的二叉树
TreeNode n = new TreeNode(1);
for (int i = 2; i <= 100000; i++) {
    n = new TreeNode(1, n, null);
}

traverse1(node, new ArrayList<>()); // StackOverflowError
traverse2(node, new ArrayList<>(), () -> {}); // StackOverflowError
StackGuard.run(() -> traverse3(node, result, r -> {}); // 没有StackOverflowError
```
