// format: off

package ch.epfl.scala.accessible

import scala.meta._

// ~;unit/testOnly ch.epfl.scala.accessible.DescribeTests;coverageReport
object DescribeTests extends DescribeTestsUtils {
  
  // Lit
  {
    import Lit._
    check[Int]("1", "1")
    check[Unit]("()", "unit")
  }

  // Term
  {
    import scala.meta.Term._


    // check[For]             ("for { x <- xs } f"                                   , "")
    // check[ForYield]        ("for { x <- xs } yield f"                             , "")
    // check[Param]           ("def f(a: A = da): A"                                 , "")
    
    check[Annotate]        (q"a: @T"                                   , "a annotated with: T")
    check[Apply]           (q"f(a)"                                    , "f applied to a")
    check[Apply]           (q"f{x}"                                    , "f applied to x")
    check[Apply]           (q"f({ case a => a })"                      , "f applied to partial function case a then a")
    check[ApplyInfix]      (q"a op b"                                  , "a op b")
    check[ApplyInfix]      (q"a op[S, T] b"                            , "a op parameterized with S, T applied to b")
    check[ApplyInfix]      (q"(a, b) op (c, d)"                        , "tuple 2 of a, b op c, d")
    check[ApplyInfix]      (q"a f ()"                                  , "a f empty arguments")
    check[ApplyType]       (q"f[S,T]"                                  , "f applied to types S, T")
    check[ApplyType]       (q"f[T]"                                    , "f applied to type T")
    check[ApplyUnary]      (q"-a"                                      , "- a")
    check[Ascribe]         (q"a: T"                                    , "a typed as T")
    check[Assign]          (q"a = b"                                   , "a assigned to b")
    check[Block]           (q"{ a; b }"                                , "a, b")
    check[Do]              (q"do d while (p)"                          , "do d while p")
    check[Eta]             (q"f _"                                     , "η-conversion of f")
    check[Function]        (q"(a, b) => c"                             , "function a, b to c")
    check[Function]        (q"() => a"                                 , "function unit to a")
    check[If]              ( "if (p) t else f"                         , "if p then t else f")
    check[If]              ( "if (p) t"                                , "if p then t")
    check[If]              ( "if (p) t else ()"                        , "if p then t else unit")
    check[If]              ( "if (p) if (p2) t"                        , "if p then if p2 then t")
    check[Interpolate]     (""" s"foo ${a} bar" """                    , "s interpolation foo insert a, bar")
    check[Match]           (q"a match { case x => x }"                 , "a match case x then x")
    check[New]             (q"new A"                                   , "new A")
    check[New]             (q"new exp.C(x, y)(z)"                      , "new select exp C applied to x, y then z")
    check[NewAnonymous]    (q"new A{}"                                 , "new anonymous A")
    check[PartialFunction] (q"{ case a => a }"                         , "partial function case a then a")
    check[Repeated]        ("f(→x: _*←)"                               , "repeated x")
    check[Return]          (q"return a"                                , "returns a")
    check[Select]          (q"a.b"                                     , "a dot b")
    check[Super]           (q"a.super[B]"                              , "a super B")
    check[Super]           (q"super[B]"                                , "super B")
    check[Super]           (q"a.super"                                 , "a super")
    check[This]            (q"a.this"                                  , "this within a")
    check[Throw]           (q"throw e"                                 , "throw e")
    check[Try]             (q"try (f) catch { case x => x } finally z" , "try f catch case x then x finally z")
    check[Try]             (q"try (f) finally z"                       , "try f finally z")
    check[TryWithHandler]  (q"try (f) catch (h) finally z"             , "try f catch h finally z")
    check[Tuple]           (q"(a, b)"                                  , "tuple 2 of a, b")
    check[While]           (q"while (p) d"                             , "while p do d")
    check[Xml]             ( "<a>b{c}d</a>"                            , "xml literal <a>b scala expression c, d</a>")
  }

  // Type
  {
    import Type._

    check[Annotate]             (t"T @A"                  , "T annotated with: A")
    check[Name]                 (t"B"                     , "B")
    check[Select]               (t"a.B"                   , "select a B")
    check[Project]              (t"a#B"                   , "project a B")
    check[Singleton]            (t"this.type"             , "singleton this")
    check[Singleton]            (t"t.type"                , "singleton t")
    check[Apply]                (t"F[T]"                  , "F of T")
    check[Apply]                (t"Map[K, V]"             , "Map applied to K, V")
    check[Apply]                (t"M[_, _]"               , "M taking 2 parameters")
    check[ApplyInfix]           (t"K Map V"               , "K Map V")
    check[Existential]          (t"T forSome { type A }"  , "existential T for some type A")
    check[Function]             (t"() => B"               , "function Unit to B")
    check[Function]             (t"A => B"                , "function A to B")
    check[Function]             (t"(A, B) => C"           , "function A, B to C")
    checkType[ImplicitFunction] ("implicit A => B"        , "implicit function A to B", dialect = dotty)
    check[Tuple]                (t"(A, B)"                , "tuple 2 of A, B")
    check[With]                 (t"A with B"              , "A with B")
    checkType[And]              ("A & B"                  , "A and B", dialect = dotty)
    checkType[Or]               ("A | B"                  , "A or B", dialect = dotty)
    check[Refine]               (t"A { def f: B }"        , "type refinements: A def f returns: B")
    check[Refine]               (t"A { }"                 , "type refinements: A")
    check[Refine]               (t"{ def f: B }"          , "type refinements: def f returns: B")
    checkType[Lambda]           ("[X] => (X, X)"          , "lambda X to tuple 2 of X, X", dialect = dotty)
    checkType[Placeholder]      ("_"                      , "placeholder")
    checkType[Bounds]           ("_ >: A <: B"            , "placeholder super-type of: A sub-type of: B")
    checkType[Bounds]           ("_ <: B"                 , "placeholder sub-type of: B")
    checkType[Bounds]           ("_ >: A"                 , "placeholder super-type of: A")
    check[ByName]               (t"=> T"                  , "by name: T")
    check[Repeated]             (t"Any*"                  , "repeated: Any")
    check[Param]                ("def f[→_ <: B←]: C"     , "a parameter sub-type of: B")
    check[Param]                ("def f[→A <% B[A]←]: C"  , "A view bounded by: B of A.")
    check[Param]                ("def f[→A: B←]: C"       , "A context bounded by: B.")
    check[Param]                ("def f[→A : B : C←]: D"  , "A context bounded by: B, C.")
  }

  // Pat
  {
    import scala.meta.Pat._

    checkPat[Bind]         ("a @ A"                   , "a bound to A")
    check[Wildcard]        (p"_"                      , "placeholder")
    check[SeqWildcard]     (p"_*"                     , "multiple placeholders")
    check[Alternative]     (p"a | b"                  , "a or b")
    check[Tuple]           (p"(a, b)"                 , "tuple 2 of a, b")
    check[Extract]         (p"E(a, b)"                , "E extracts a, b")
    check[ExtractInfix]    (p"a :: b"                 , "a :: b")
    checkPat[Interpolate]  (""" s"foo $a bar $b" """  , "s interpolation foo extracts a, bar extracts b")
    checkPat[Xml]          ("<h1>a{b}c{d}e{f}g</h1>"  , "xml interpolation <h1>a extracts b, c extracts d, e extracts f, g</h1>")
    checkPat[Typed]        ("x: T"                    , "x typed as T")
    check                  (p"case `foobar` => rhs"   , "case exactly foobar then rhs")
  }

  // Decl
  {
    import Decl._
    check[Def] ( "def f"                       , "def f")
    check[Def] (q"def f: String"               , "def f returns: String")
    check[Type](q"type T"                      , "type T")
    check[Type](q"private type Foo[A, B] <: D" , "private type Foo parameterized with: A, B. sub-type of: D")
    check[Val] (q"val a: Int"                  , "val a Int")
    check[Var] (q"var a: Int"                  , "var a Int")
  }


  // Defn
  {
    import Defn._
    check[Class] (q"class A"                                     , "class A")
    check[Class] (q"class A[T]"                                  , "class A parameterized with: T")
    check[Class] (q"class A extends B with C with D"             , "class A extends B with C with D")
    check[Class] (q"class A extends { val a = 1 } with B with C" , "class A early initializer: val a = 1 with B with C")
    check[Def]   (q"def f: String = 1"                           , "def f returns: String body: 1")
    check[Def]   (q"def f(a: A, b: B)(c: C): E = e"              , "def f a A, b B, c C returns: E body: e")
    check[Macro] (q"def f: Int = macro impl"                     , "def f returns: Int macro body: impl")
    check[Object](q"object A"                                    , "object A")
    check[Trait] (q"trait A"                                     , "trait A")
    check[Type]  (q"type T = Int"                                , "type T Int")
    check[Type]  (q"private type Foo[A] = Int"                   , "private type Foo parameterized with: A Int")
    check[Val]   (q"val a = 1"                                   , "val a = 1")
    check[Val]   (q"val a: Int = 1"                              , "val a of type: Int = 1")
    check[Val]   (q"val (x, y) = (1, 2)"                         , "val tuple 2 of x, y = tuple 2 of 1, 2")
    check[Var]   (q"var a = 1"                                   , "var a = 1")
  }

  // Mod
  {
    import Mod._

    check[Abstract]      ("→abstract← class A"         , "abstract")
    check[Contravariant] ("class A[→-T←]"              , "contra-variant T")
    check[Covariant]     ("class A[→+T←]"              , "co-variant T")
    check[Private]       ("→private← class A"          , "private")
    check[Private]       ("→private[scope]← class A"   , "private within scope")
    check[Private]       ("→private[this]← class A"    , "private this")
    check[Protected]     ("→protected← class A"        , "protected")
    check[Protected]     ("→protected[scope]← class A" , "protected within scope")
    check[Protected]     ("→protected[this]← class A"  , "protected this")
  }

  // Enumerator

  // Ctor

  // Import
  {
    check[Import](q"import a.b"        , "import: a b")
    check[Import](q"import a.b, c.d"   , "import: a b, c d")
    check[Import](q"import a._"        , "import: a wildcard")
    check[Import](q"import a.{b => _}" , "import: a unimport b")
    check[Import](q"import a.{b => c}" , "import: a rename b to c")
  }

  // Misc
  {
    check[Pkg]("package a.b { class C }", "package a dot b class C" )
    check[Pkg.Object]("package object a extends B", "package object a extends B" )
    // Init
    // Self
    check[Self]("trait A { →foo: T← => }", "self type foo typed as: T")



  }
}
