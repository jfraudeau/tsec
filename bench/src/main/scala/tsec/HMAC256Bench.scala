package tsec

import java.util.concurrent.TimeUnit

import cats.effect.IO
import org.openjdk.jmh.annotations._
import tsec.common._
import tsec.mac.imports._
import tsec.libsodium.ScalaSodium
import tsec.libsodium.authentication._

import scala.util.Random

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class HMAC256Bench {

  implicit lazy val sodium = ScalaSodium.getSodiumUnsafe
  lazy val lsKey           = HS256.generateKey[IO].unsafeRunSync()
  lazy val key             = HMACSHA256.generateKeyUnsafe()
  lazy val rand            = new Random()
  lazy val longPlaintext   = Array.fill[Char](5000)(Random.nextInt(127).toChar).mkString.utf8Bytes
  lazy val jca             = JCAMac[IO, HMACSHA256]

  @Benchmark
  def testJCA(): Unit =
    (for {
      o     <- jca.sign(longPlaintext, key)
      verif <- jca.verify(longPlaintext, o, key)
    } yield assert(verif)).unsafeRunSync()

  @Benchmark
  def testLibSodium(): Unit =
    (for {
      o     <- HS256.sign[IO](longPlaintext, lsKey)
      verif <- HS256.verify[IO](longPlaintext, o, lsKey)
    } yield assert(verif)).unsafeRunSync()

}
