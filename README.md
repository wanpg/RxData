# RxData
RxJava实现的像LiveData一样有生命周期感知的可被观察的对象

[![](https://jitpack.io/v/wanpg/RxData.svg)](https://jitpack.io/#wanpg/RxData)


### 引用方式
```groovy
implementation 'com.github.wanpg:rxdata:0.0.4-SNAPSHOT'
implementation "io.reactivex.rxjava2:rxjava:2.1.12"
implementation 'io.reactivex.rxjava2:rxandroid:2.0.2'
```

### 使用方式
##### 1. 定义一个数据

类似`Observable`和`LiveData`

```kotlin
var dataRx = RxData<String>()
```

##### 2. 设置或者获取数据

```kotlin
// 设置数据，支持Null
dataRx.set("Hello RxData")
// 获取数据
var str = dataRx.get()
```

##### 3. 设置Rx订阅

​	为了方便看清楚返回类型，没用lambda语法。`onNext`方法将会返回一个`DataWrap`的数据包装类，可以判断数据是否为空

```kotlin
dataRx.observable(lifecycleowner)
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(object : Consumer<RxData.DataWrap<String>> {
        override fun accept(t: RxData.DataWrap<String>?) {
            if (t!!.isNull) {
                System.out.println("获取了一个Null值")
            } else {
                val str = t.get()
            }
        }
    })
```
