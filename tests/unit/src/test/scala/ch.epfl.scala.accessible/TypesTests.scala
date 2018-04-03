// package ch.epfl.scala.accessible

// object TypesTests extends TestSuite with DiffAssertions {
//   val tests = Tests {
//     "*" - {

//     }
//   }
// }

// // checkType("B")                    // Type.Name
// // checkType("a.B")                  // Type.Select
// // checkType("a#B")                  // Type.Project
// // checkType("this.type")            // Type.Singleton
// // checkType("t.type")               // Type.Singleton
// // checkType("F[T]")                 // Type.Apply
// // checkType("K Map V")              // Type.ApplyInfix
// // checkType("() => B")              // Type.Function
// // checkType("A => B")               // Type.Function
// // checkType("(A, B) => C")          // Type.Function
// // checkType("implicit A => B")      // Type.ImplicitFunction
// // checkType("(A, B)")               // Type.Tuple
// // checkType("A with B")             // Type.With
// // checkType("A & B")                // Type.And
// // checkType("A | B", dotty)         // Type.Or
// // checkType("A { def f: B }")       // Type.Refine
// // checkType("A{}")                  // Type.Refine
// // checkType("{ def f: B }")         // Type.Refine
// // checkType("A forSome { type T }") // Type.Existential
// // checkType("T @A")                 // Type.Annotate
// // checkType("[X] => (X, X)")        // Type.Lambda
// // checkType("_")                    // Type.Placeholder

// // checkType("_ >: A <: B")          // Type.Bounds
// // checkType("_ <: B")               // Type.Bounds (lower)
// // checkType("_ >: A")               // Type.Bounds (upper)
// // check("def f[A <% B[A]]: C")      // Type.Bounds (view)
// // check("def f[A: B]: C")           // Type.Bounds (context)
// // check("def f[A : B : C]: D")      // Type.Bounds (context)

// // checkType("=> T")                 // Type.ByName
// // checkType("Any*")                 // Type.Repeated
// // check("trait A[X]")               // Type.Param
// // check("def f[@a A]: B")           // Type.Param (annotations)
// // checkPat("List[t](xs @ _*)")      // Type.Var