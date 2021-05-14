# 前言
&emsp; 现在混合式的移动应用已经越来越多，像支付宝、微信、京东等主流App，里面都嵌入了很多H5应用。这些H5应用本质上还是一个个网页，基于手机的WebView渲染运行。<br>
&emsp;虽然这样达到了跨平台的效果，但是，这些网页是没有直接调用原生设备的能力的。<br>
&emsp;基于此，JS Bridge桥就成了H5应用与原生通讯的关键。
# 1 JS Bridge是什么
&emsp; JS Bridge实际上就是一段JS代码，里面封装好`H5访问原生的方法`，`原生访问H5的方法`，即可作为一个“桥”架在 `H5` 和 `原生` 中间。
# 2 JS Bridge技术实现
> 要实现JS Bridge，我们需要按以下步骤进行：<br>
> （注：本篇文章基于Android6以上，以阐述原理为主）<br>

- 第一步：定义一个Native与JS交互的全局桥对象
- 第二步：JS调用Native
- 第三步：Native接收JS传递过来的参数
- 第四步：Native返回数据给JS
- 第五步：JS接收原生传递过来的参数
- 第六步：回调事件处理
### 2.1 全局桥对象
定义全局桥对象。<br>
后续，我们需要用它来访问我们定义的与原生“通讯”方法。
```javascript
var JSBridge = window.JSBridge || (window.JSBridge = {})
```
### 2.2 JS调用原生
关于JS如何主动调用原生，Android官方封装了接口。<br>
首先需要创建JS访问原生的命名空间：<br>
`webView.addJavascriptInterface(this, "androidPlatform");` <br>
关于这个方法，官方给予了解释，
> Injects the supplied Java object into this WebView. The object is injected into all frames of the web page, including all the iframes, using the supplied name. <br>
> 将提供的Java对象注入到此WebView中。将提供的命名空间注入到网页的所有框架中，包括所有iframe。这允许从JavaScript访问Java对象的方法。<br>

Android这边定义好命名空间后，JS就可以通过该命名空间传递数据给原生：
```javascript
window.androidPlatform.postString(jsonStr); // 这样就将参数传给了原生的postString方法
```
### 2.3 Native接收JS传递过来的参数
在传给`webView`的Java对象中创建`postString`方法，接收JS传递的数据。
```java
@JavascriptInterface // 从Android 4.2开始必须要加上该注解，postString方法才能被JS访问
public void postString(String jsonStr) {
    // jsonStr就是JS传递过来的数据
}
```
### 2.4 Native返回数据给JS
Native如何返回数据给JS，Android的webView提供了方法。
```java
final String script = "javascript:window.JSBridge.postMsg(" + data + ")";
webView.evaluateJavascript(script, null));
```
### 2.5 JS接收原生传递过来的参数
```java
// JS定义window,JSBridge.postMsg方法接收数据
JSBridge.postMsg = function postMsg(result) {
    // 处理返回数据
}
```
### 2.6 回调事件处理
一般而言，JS调用原生方法，都希望得到返回数据。<br>
所以JSBridge最好可以返回一个异步的`Promise`给调用者，等到原生返回数据后，通过该Promise接收到数据。
```javascript
// 通过JSBridge.call调用原生
window.JSBridge.call(data).then();

// JSBridge.call()返回Promise
JSBridge.call = function (data) {
  // 返回Promise
  return new Promise(function (resolve, reject) {
    JSBridge.callNative(data, {
      resolve: resolve,
      reject: reject
    });
  });
};

// 在这里执行真正的访问操作
// callbacks={} 缓存所有回调事件
JSBridge.callNative = function callNative(data, responseCallback) {
  try {
    var callbackId = '0';
    if (callbacks && typeof responseCallback.resolve === 'function') {
      callbackId = 'cb_' + (uniqueId++) + '_' + new Date().getTime(); // 生成唯一的callbackId
      callbacks[callbackId] = responseCallback; // 保存回调事件
    }
    // 调原生
    // 执行2.2的操作访问原生
  } catch (e) {
    console.error(e);
  }
  return null;
}

// 结束后，原生返回数据给JSBridge，参考 2.4 、 2.5
// 如果前端想要在then()里得到异步结果，还需要在 2.5 (JSBridge.postMsg) 
// 中通过callbackId取回调事件执行
JSBridge.postMsg = function postMsg(result) {
  // 处理返回数据
  var call = callbacks[result.callbackId]; // callbackId需要全程“跟随”此次调用
  // call.resolve(result.data); // 成功走resolve
  // call.reject(result.error); // 失败走reject
  // 如果看不懂这里，可以去了解一下JS的Promise。
}
```
# 3 写在最后
&emsp; JS Bridge实现起来还是不难的，本质就是调用Android/iOS原生平台提供的JS调用接口，做一些接口的封装。<br>
&emsp; 如果上面的代码，各位觉得看着比较凌乱，可以去看下示例demo中的完整代码。
&emsp; [JSBridgeDemo](https://github.com/halloayu/JSBridgeDemo.git)
