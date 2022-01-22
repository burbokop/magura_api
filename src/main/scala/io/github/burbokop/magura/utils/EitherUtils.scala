package io.github.burbokop.magura.utils

object EitherUtils {
  class ListImplicits[A, B](list: List[Either[A, B]]) {
    def partitionEither: Either[List[A], List[B]] =
      list.partition(_.isLeft) match {
        case (Nil,  ints) => Right(for(Right(i) <- ints) yield i)
        case (strings, _) => Left(for(Left(s) <- strings) yield s)
      }

    def filterLefts: List[A] = list.filter(_.isLeft).map(_.left.toOption.get)
    def filterRights: List[B] = list.filter(_.isRight).map(_.toOption.get)
  }

  object ListImplicits {
    implicit def apply[A, B](list: List[Either[A, B]]) = new ListImplicits[A, B](list)
  }

  class ThrowableListImplicits[B](list: List[Either[Throwable, B]]) {
    def reducedPartitionEither: Either[ReducedError, List[B]] =
      ListImplicits(list).partitionEither.left.map(ReducedError(_))
  }

  object ThrowableListImplicits {
    implicit def apply[B](list: List[Either[Throwable, B]]) = new ThrowableListImplicits[B](list)
  }
}