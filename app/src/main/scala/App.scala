
import boopickle.Default._
import org.scalajs.dom
import org.scalajs.dom.Worker

import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import scala.scalajs.js.typedarray.{ ArrayBuffer, TypedArrayBuffer }

case class Foo(n: Int, s: String)

object App {
  val worker = new Worker("worker.js")
  def main(args: Array[String]): Unit = {
    println("main()")

    worker.onmessage = (msg: dom.MessageEvent) ⇒ {
      val foo = Serde[Foo](msg)
      println(s"received doubled foo: $foo")
    }

    val foo = Foo(123, "abc")
    worker.postMessage(Serde(foo))
  }
}

object Serde {
  def apply[T](t: T)(implicit pickler: Pickler[T]): ArrayBuffer = {
    val data = Pickle.intoBytes(t)
    assert(data.hasTypedArray())
    data.typedArray.buffer.slice(0, data.limit())
  }
  def apply[T](msg: dom.MessageEvent)(implicit pickler: Pickler[T]): T = apply(msg.data.asInstanceOf[ArrayBuffer])(pickler)
  def apply[T](data: ArrayBuffer)(implicit pickler: Pickler[T]): T = {
    val buffer = TypedArrayBuffer.wrap(data)
    println(s"received response: ${data.byteLength}")
    Unpickle[T].fromBytes(buffer)
  }
}
