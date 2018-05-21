package ch.epfl.scala.accessible

object CursorPlayground extends CursorTestsUtils {
  test("playground") { 
    doFocus(
      "package a.b",
      ("→package a.b←", down),
      ("package →a.b←", down),
      ("package a.→b←", down),
      // ("package a.→b←", down)
    )    
  }
}