import org.scalajs.dom
import org.scalajs.dom.Worker

import scala.scalajs.js

case class Foo(n: Int, s: String)

object MainApp {
  val worker = new Worker("worker.js")
  def main(args: Array[String]): Unit = {
    println("main()")
    worker.onmessage = (e: dom.MessageEvent) ⇒ {
      println(s"received response: $e (${e.data})")
      val foo = e.data.asInstanceOf[Foo]
      println(s"foo: $foo")
    }

    worker.postMessage(Foo(123, "abc").asInstanceOf[js.Any])
  }
}
