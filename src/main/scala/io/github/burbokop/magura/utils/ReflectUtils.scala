package io.github.burbokop.magura.utils

import scala.reflect.ClassTag
import scala.reflect.runtime.universe

object ReflectUtils {
  implicit class MirrorImplicits(mirror: universe.Mirror) {
    def instanceType[T: ClassTag](instance: T): universe.Type =
      mirror.reflect(instance).symbol.toType

    def annotations(`class`: String): List[universe.Annotation] =
      mirror.staticClass(`class`).annotations

    def invokeAttachedMethod[A, R](`class`: String, arg: A)(implicit aTag: universe.TypeTag[A], rTag: universe.TypeTag[R]): Option[R] =
      findAnnotationMethod(`class`, universe.typeOf[A], universe.typeOf[R]).flatMap(method => {
        methodModule(method).map(module => {
          mirror.reflect(mirror.reflectModule(module.symbol.asModule).instance)
            .reflectMethod(method.symbol.asMethod)(arg).asInstanceOf[R]

        })
      })

    def methodModule(method: universe.Tree): Option[universe.Tree] = {
      var result: Option[universe.Tree] = None
      method.children.exists(child => {
        child.children.exists(childOfChild => {
          if (childOfChild.symbol.isModule) {
            result = Some(childOfChild)
            true
          } else false
        })
      })
      result
    }

    def findAnnotationMethod(`class`: String, argType: universe.Type, resType: universe.Type): Option[universe.Tree] = {
      //println("A0")
      var result: Option[universe.Tree] = None
      annotations(`class`).find(annotation => {
        //println(s"\tA1: $annotation")
        annotation.tree.children.find(child => {
          //println(s"\t\tA2: $child -> ${Option(child.symbol).map(_.isMethod)}")
          child.children.find(childOfChild => {
            //println(s"\t\t\tA3: $childOfChild")
            if (Option(childOfChild.symbol).exists(_.isMethod)) {
              val method = childOfChild.symbol.asMethod
              //println(s"\t\t\tA4: $method")
              if (method.paramLists.exists(paramList => paramList.length == 1 && paramList.head.typeSignature <:< argType) && method.typeSignature.resultType <:< resType) {
                result = Some(childOfChild)
                true
              } else false
            } else false
          })
          result.isDefined
        })
        result.isDefined
      })
      result
    }

  }
}