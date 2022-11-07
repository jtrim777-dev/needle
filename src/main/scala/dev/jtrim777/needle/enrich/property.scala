package dev.jtrim777.needle.enrich

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("Enable the `-Ymacro-annotations` flag to use compile-time annotations")
class property extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro propertyO.impl
}

object propertyO {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs = annottees.map(_.tree).toList

    println(inputs)

    annottees.head
  }
}
