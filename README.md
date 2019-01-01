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

## How to serialize Scala objects over Worker API?
The app currently fails to pass a Scala case class over the API because I don't know the preferred way to serialize it for that journey.

At http://localhost:8000/ you should see something in the dev console like:

[![](https://cl.ly/9b83c8a8bae2/Screen%20Shot%202019-01-01%20at%204.17.37%20PM.png)](https://cl.ly/9b83c8a8bae2/Screen%20Shot%202019-01-01%20at%204.17.37%20PM.png)

Naively passing a [`Foo`](app/src/main/scala/MainApp.scala#L6) over the web-worker API:

```scala
worker.postMessage(Foo(123, "abc").asInstanceOf[js.Any])
```

throws that cast exception on the receiving end:

```scala
object Worker {
  def onMessage(msg: dom.MessageEvent) = {
    val foo = msg.data.asInstanceOf[Foo]  // 💥
    …
  }
}
```

What is the best way to send regular Scala ADTs over the Worker API (or other similar `js.Any`-based APIs)? 

## Separate SBT module for worker app

[Existing scalajs web-worker references I found](https://gist.github.com/ochrons/e4df27f1e6a56912471db163ef8eff73) don't account for problems that scalajs-bundler introduces. 

For example, the worker must run from a separate  "`worker.js`" script without a `window` object, and that goes through a different top-level entry-point than your main app, but [scalajs-bundler removes top-level exports](https://scalacenter.github.io/scalajs-bundler/cookbook.html#several-entry-points).

I worked around this by making the worker "application" its own SBT module (`worker`, in [the `app/worker` directory](app/worker)).

Nesting it under the main `app` module's directory made it easier for relevant scripts to see each other; for example, [`app`'s main object](app/src/main/scala/App.scala#L8) needs to run [`worker.js`](app/worker.js), which in turn needs to import [the `worker`'s generated JS application](app/worker/src/main/scala/Worker.scala) (`app/worker/target/scala-2.12/worker-fastopt.js`).

### Using scalajs-bundler in the worker module?
In this example, `worker` doesn't use scalajs-bundler, but in principle it might be able to, if you wanted it to use some NPM dependencies. However, that might usually fail; I think the worker's global scope doesn't include e.g. `require`, which is needed to load such dependencies
