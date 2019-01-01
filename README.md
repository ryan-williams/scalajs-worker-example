# scalajs-worker-example
Proof of concept using web workers with scalajs (in particular, with scalajs-bundler)

```
git clone git@github.com:ryan-williams/scalajs-worker-example.git
cd scalajs-worker-example
sbt worker/fastOptJS app/fastOptJS::webpack
python -m http.server
```

Then go to http://localhost:8000/; you should see something in the dev console like:

[![](https://cl.ly/9b83c8a8bae2/Screen%20Shot%202019-01-01%20at%204.17.37%20PM.png)](https://cl.ly/9b83c8a8bae2/Screen%20Shot%202019-01-01%20at%204.17.37%20PM.png)

Naively passing a [`Foo`](app/src/main/scala/MainApp.scala#L6) over the web-worker API:

```scala
worker.postMessage(Foo(123, "abc").asInstanceOf[js.Any])
```

throws a cast exception on the receiving end:

```scala
object Worker {
  def onMessage(msg: dom.MessageEvent) = {
    val foo = msg.data.asInstanceOf[Foo]  // 💥
    …
  }
}
```

What is the best way to send regular Scala ADTs over the Worker API (or other similar `js.Any`-based APIs)? 
