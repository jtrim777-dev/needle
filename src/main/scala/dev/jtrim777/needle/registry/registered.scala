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

    val valDef = inputs.head match {
      case field: ValDef => field
      case _ => throw new IllegalArgumentException("The @registered macro may only be used on lazy value definitions")
    }

    if (!valDef.mods.hasFlag(Flag.LAZY) | valDef.mods.hasFlag(Flag.PARAM)) {
      throw new IllegalArgumentException("The @registered macro may only be used on lazy value definitions")
    }

    val varName = valDef.name.decodedName.toString
    val snaked = varName.replaceAll("([a-zA-Z0-9])([A-Z])", "$1_$2").toLowerCase
    val nameAsTree = Literal(Constant(snaked))

    val rhs = valDef.rhs

    val trueDef = q"""
        lazy val ${valDef.name} = this.registry.get(new net.minecraft.util.Identifier(this.namespace, $nameAsTree))
        this.addEntry($nameAsTree, $rhs)"""

    c.Expr[Any](trueDef)
  }
}
