package app

import java.net._
import java.io._
import scala.io._

object Client {
	def main(args: Array[String]): Unit = {
		println("Client")
		
		val s = new Socket(InetAddress.getByName("localhost"), 5000)
		lazy val in = new BufferedSource(s.getInputStream()).getLines()
		val out = new PrintStream(s.getOutputStream())
		
		out.println("associar")
		out.flush()
		
		val response = in.next()
		println("\nClient Received: " + response)
		
		val addrOperation, asd = response.split(":")(0)
		
		println(addrOperation + " / " + asd)
		
		s.close()
	}
}