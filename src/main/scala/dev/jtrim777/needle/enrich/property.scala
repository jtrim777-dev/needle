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

    val valDef = inputs.head match {
      case field: ValDef => field
      case _ => c.abort(c.enclosingPosition, "The @property macro may only be used on value definitions")
    }

    if (valDef.mods.hasFlag(Flag.PARAM)) {
      c.abort(c.enclosingPosition, "The @property macro may only be used on value definitions")
    }

    if (valDef.tpt.isEmpty) {
      c.abort(c.enclosingPosition, "Values must explicitly define a type to be used as properties")
    }

    val varName = valDef.name.decodedName.toString
    val nameAsTree = Literal(Constant(varName))

    val rhs = valDef.rhs

    val trueDef =
      q"""
        ${valDef.mods} val ${valDef.name} = { (eis:dev.jtrim777.needle.enrich.EnrichedItemStack) => eis.load[${valDef.tpt}]($nameAsTree) }
        this.registerEnrichment($nameAsTree, { _ => $rhs })"""

    c.Expr[Any](trueDef)
  }


}
