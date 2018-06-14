object Playground {
  /*

  Welcome to Accessible Scala !

  Use the following keyboard shortcuts to navigate the code:

    Navigate and Describe  Alt-Down, Alt-Right, Alt-Left, Alt-Up
    Describe               Ctrl-D
    Summarize              Ctrl-S
    Breadcrumbs            Ctrl-B
    Misc
      Toggle Dark/Light    F2
      Toggle Speech        F3

   */

  // == Terms ==

  f()
  f(x, y)
  f(x: _*)
  (x, y, z)
  f { case x => y }

  x op y
  x op [S, T] y
  (x, y) op (r, s)
  x f ()
  f[S, T]
  f[T]

  x.y
  -x
  x = y

  new S
  new e.S(x, y)(z)
  new S {}

  do d while (p)
  while (p) d
  for (x <- f) yield x
  for (x <- f; y <- g; z = x; if p(z)) f

  throw e
  try (f)
  catch { case x => x } finally z
  try (f)
  finally z
  try (f)
  catch (h)
  finally z

  x match { case y => y }

  if (p) t else f
  if (p) t
  if (p) t else ()
  if (p) if (p2) t

  x: T
  x: @T

  val g = f _
  val g = (x, y) => x
  val g = () => x

  s"foo ${x} bar"

  // == Types ==
  f[e.T]
  f[S#T]

  f[List[T]]
  f[Map[K, V]]
  f[Map[_, _]]
  f[K Map V]
  f[() => S]
  f[S => T]
  f[(R, S) => T]
  f[(S, T)]
  f[S with T]
  f[S { def f: T }]
  f[S {}]
  f[{ def f: S }]
  def f[_ >: S <: T]
  def f(f: => T)
  def f(ts: T*)
  def f[_ <% T]
  def f[_: B: C]
  trait S { def me: this.type }

  // == Patterns ==

  z match {
    case x @ S                  =>
    case List(_*)               =>
    case x | y                  =>
    case (x, y)                 =>
    case E(x, y)                =>
    case x :: y                 =>
    case s"foo $x bar $y"       =>
    case <h1>s{x}s{y}s{z}s</h1> =>
    case x: S                   =>
    case `x`                    =>
    case _                      =>
  }

  // == Declarations ==

  trait Declarations {
    def f
    def f: String
    type T
    val x: Int
    var y: Int
  }

  // == Definitions ==
  class S
  class S[T1, T2] extends X with Y
  class S extends { val x = 1 } with B with C
  class S { def this(a: Int) { this() } }
  class S(x: Long) { def this(y: Int) { this(0L); g() } }
  trait S { x: T =>
  }

  def f = 1
  def f: Int = 1
  def f(a: A, b: B)(c: C): E = e
  def f: Int = macro impl
  object A
  trait A
  type T = Int
  private type Foo[A] = Int
  val a = 1
  val a: Int = 1
  val (x, y) = (1, 2)
  var a = 1

  // == Modifiers ==
  abstract class S
  class S[-T]
  class S[+T]
  private class S
  private[x] class S
  private[this] class S
  protected class S
  protected[x] class S
  protected[this] class S

  // == Imports ==
  import x.y
  import x.y, x.z
  import x._
  import x.{y => _}
  import x.{y => z}
}

// == Packages ==
package x.y { class C }
package object x extends B
