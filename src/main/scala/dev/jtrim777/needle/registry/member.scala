package dev.jtrim777.needle.registry

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("Enable the `-Ymacro-annotations` flag to use compile-time annotations")
class member extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro memberHolder.impl
}

object memberHolder {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs = annottees.map(_.tree).toList

    val valDef = inputs.head match {
      case field: ValDef => field
      case _ => throw new IllegalArgumentException("The @member macro may only be used on lazy value definitions")
    }

    if (!valDef.mods.hasFlag(Flag.LAZY) | valDef.mods.hasFlag(Flag.PARAM)) {
      throw new IllegalArgumentException("The @member macro may only be used on lazy value definitions")
    }

    val strLit = Literal(Constant(valDef.name.decodedName.toString))
    val trueDef = q"""lazy val ${valDef.name} = this.registry.get(new net.minecraft.util.Identifier(this.namespace, $strLit))"""

    c.Expr[Any](trueDef)
  }
}
