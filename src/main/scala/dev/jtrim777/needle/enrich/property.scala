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

    val (param: ValDef, clazz: ClassDef, other) = inputs match {
      case (p: ValDef) :: (c : ClassDef) :: rest => (p, c, rest)
      case _ => c.abort(c.enclosingPosition, "The @property macro may only be used on class fields")
    }

    clazz match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
        println(mods)
        println(tpname)
        println(tparams)
        println(ctorMods)
        println(paramss)
        println(earlydefns)
        println(parents)
        println(self)
        println(stats)
      case other => println("FUCK: "+other)
    }



    c.Expr[Any](clazz)
  }


}
