package dev.jtrim777.needle.registry

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.whitebox
import scala.language.experimental.macros

@compileTimeOnly("Enable the `-Ymacro-annotations` flag to use compile-time annotations")
class registry extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro registryO.impl
}

object registryO {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def extractModule(raw: ModuleDef): (TermName, Seq[Tree], Seq[Tree]) = {
      raw match {
        case q"object $mname extends ..$parents { ..$body }" => (
          mname.asInstanceOf[TermName],
          parents.asInstanceOf[Seq[Tree]],
          body.asInstanceOf[Seq[Tree]])
      }
    }

    def mapField(vd: ValDef, mt: Type): Seq[Tree] = {
      if (!vd.mods.hasFlag(Flag.LAZY) || vd.mods.hasFlag(Flag.PARAM)) {
        c.abort(vd.pos, "The @registered macro may only be used on lazy object fields")
      } else {
        val modMods = vd.mods.mapAnnotations(_.flatMap {
          case Apply(q"new registered", _) => None
          case o => Some(o)
        })

        val varNameRaw = vd.name.decodedName.toString
        val snaked = varNameRaw.replaceAll("([a-zA-Z0-9])([A-Z])", "$1_$2").toLowerCase
        val varName = Literal(Constant(snaked))

        val newDefn = if (vd.tpt.isEmpty) {
          val nrhs = q"this.lookup($varName)"
          ValDef(modMods, vd.name, vd.tpt, nrhs)
        } else {
          val ttpt = c.typecheck(vd.tpt, mode = c.TYPEmode)
          if (!(ttpt.tpe <:< mt)) {
            c.abort(vd.pos, s"Cannot register value as type `${vd.tpt}` as it does not conform to member type `$mt`")
          }

          val nrhs = q"this.lookup($varName).asInstanceOf[${vd.tpt}]"
          ValDef(modMods, vd.name, vd.tpt, nrhs)
        }

        val valueExpr = if (vd.tpt.isEmpty) {
          q"${vd.rhs}.asInstanceOf[$mt]"
        } else {
          q"${vd.rhs}.asInstanceOf[${vd.tpt}]"
        }

        val setIt = q"this.addEntry($varName, $valueExpr)"
        List(setIt, newDefn)
      }
    }

    def findMemberType(parents: Seq[Tree]): Type = {
      val typedParents = parents.map(t => c.typecheck(t, mode = c.TYPEmode))

      val selected = typedParents.find { tt =>
        tt.tpe <:< typeOf[DelayedRegistry[_]]
      }.getOrElse(c.abort(c.enclosingPosition, "@registry annotated objects must extend DelayedRegistry"))

      val selTyp = selected.tpe.dealias
      val memTyp = selTyp.typeArgs.head

      memTyp
    }

    val inputs = annottees.map(_.tree).toList

    val module = inputs.head match {
      case module: ModuleDef => module
      case _ => c.abort(c.enclosingPosition, "The @registry macro can only be used on objects")
    }

    val (mname, mparents, mbody) = extractModule(module)

    val memberType = findMemberType(mparents)

    val relevance = mbody.map {
      case vd @ ValDef(Modifiers(_, _, annots), _, _, _) if annots.exists {
        case Apply(q"new registered", _) => true
        case _ => false
      } => Right(vd)
      case o => Left(o)
    }

    val results = relevance.flatMap {
      case Left(value) => List(value)
      case Right(value) => mapField(value, memberType)
    }

    c.Expr[Any](
      q"""
         object $mname extends ..$mparents {
             ..$results
         }
       """
    )
  }
}
