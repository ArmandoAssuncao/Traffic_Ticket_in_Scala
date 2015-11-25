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
		
		out.println("remover")
		out.flush()
		
		val response = in.next()
		println("\nClient Received: " + response)
		
		val (addrOperation, port) = (response.split(":")(0), response.split(":")(1))
	
		s.close()
	}
}