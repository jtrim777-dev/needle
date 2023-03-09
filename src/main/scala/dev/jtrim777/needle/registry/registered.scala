package dev.jtrim777.needle.registry

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("Enable the `-Ymacro-annotations` flag to use compile-time annotations")
class registered extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro registeredO.impl
}

object registeredO {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs = annottees.map(_.tree).toList

    c.abort(c.enclosingPosition, "The @registered macro may only be used inside @registry-annotated objects")

    c.Expr[Any](inputs.head)
  }
}
