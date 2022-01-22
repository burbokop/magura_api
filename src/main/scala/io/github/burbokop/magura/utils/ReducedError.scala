package io.github.burbokop.magura.utils

case class ReducedError(
                         errors: List[Throwable]
                       ) extends Exception("error components:\n" + errors.reduce((a: Throwable, b: Throwable) => new Exception(s"\t$a\n\t$b")))
