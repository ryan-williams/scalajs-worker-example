# scalajs-worker-example
Proof of concept using web workers with scalajs (in particular, with scalajs-bundler), and discussion of some issues.

## Setup

```
# clone
git clone git@github.com:ryan-williams/scalajs-worker-example.git
cd scalajs-worker-example

# build
sbt worker/fastOptJS app/fastOptJS::webpack

# serve
cd app  # index.html and worker.js live here
python -m http.server
```

The app is then running at http://localhost:8000/

## Serializing Scala objects over the Worker API

[The `Serde` object](app/src/main/scala/App.scala#L26-38) shows a way of sending Scala objects over the Worker API by serializing to bytes using [boopickle](https://github.com/suzaku-io/boopickle), and dealing with JS byte-arrays using [the example of suzaku](https://github.com/suzaku-io/suzaku/blob/c1de595f5dba3277e3847837b719cfda5c1d6e0e/platform/web/core-shared/src/main/scala/suzaku/platform/web/WebWorkerTransport.scala#L27-L34).

[The main `App`'s send and receive code](app/src/main/scala/App.scala#L16-22) is then straightforward:

```scala
worker.onmessage = (msg: dom.MessageEvent) ⇒ {
  val foo = Serde[Foo](msg)
  println(s"received foo: $foo")
}

val foo = Foo(123, "abc")
worker.postMessage(Serde(foo))
```

and [the `Worker`'s](app/worker/src/main/scala/Worker.scala#L19-23) is similar:

```scala
def onMessage(msg: dom.MessageEvent) = {
  val foo = Serde[Foo](msg)
  val doubled = foo.copy(n = 2*foo.n)
  WorkerGlobal.postMessage(Serde(doubled))
}
``` 

## Separate SBT module for worker app

[Existing scalajs web-worker references I found](https://gist.github.com/ochrons/e4df27f1e6a56912471db163ef8eff73) don't account for problems that scalajs-bundler introduces. 

For example, the worker must run from a separate  "`worker.js`" script without a `window` object, and that goes through a different top-level entry-point than your main app, but [scalajs-bundler removes top-level exports](https://scalacenter.github.io/scalajs-bundler/cookbook.html#several-entry-points).

I worked around this by making the worker "application" its own SBT module (`worker`, in [the `app/worker` directory](app/worker)).

Nesting it under the main `app` module's directory made it easier for relevant scripts to see each other; for example, [`app`'s main object](app/src/main/scala/App.scala#L8) needs to run [`worker.js`](app/worker.js), which in turn needs to import [the `worker`'s generated JS application](app/worker/src/main/scala/Worker.scala) (`app/worker/target/scala-2.12/worker-fastopt.js`).

### Using scalajs-bundler in the worker module?
In this example, `worker` doesn't use scalajs-bundler, but in principle it might be able to, if you wanted it to use some NPM dependencies. However, that might usually fail; I think the worker's global scope doesn't include e.g. `require`, which is needed to load such dependencies
