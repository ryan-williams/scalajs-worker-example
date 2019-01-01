import boopickle.Default._
import org.scalajs.dom

import scala.scalajs.js

@js.annotation.JSGlobalScope
@js.native
object WorkerGlobal extends js.Any {
  def addEventListener(`type`: String, f: js.Function): Unit = js.native
  def postMessage(data: js.Any): Unit = js.native
}

object Worker {
  def main(args: Array[String]): Unit = {
    println("Worker.main()")
    WorkerGlobal.addEventListener("message", onMessage _ )
  }

  def onMessage(msg: dom.MessageEvent) = {
    val foo = Serde[Foo](msg)
    val doubled = foo.copy(n = 2*foo.n)
    WorkerGlobal.postMessage(Serde(doubled))
  }
}
