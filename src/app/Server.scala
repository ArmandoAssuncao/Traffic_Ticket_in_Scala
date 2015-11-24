package app

import java.net._
import java.io._
import scala.io._

object Server {
	def main(args: Array[String]): Unit = {
		val addr = InetAddress.getByName("localhost")
		val port = 9999
		
		val server = new ServerSocket(port, 50, addr)
		println("Server running in: " + server.getInetAddress().getHostAddress + ":" + server.getLocalPort)
		
		while (true) {
		    val s = server.accept()
		    val in = new BufferedSource(s.getInputStream()).getLines()
		    val out = new PrintStream(s.getOutputStream())
		    
		    println("\nServer Received: " + in.next())
		    
		    out.println("You not a special")
		    out.flush()
		    s.close()
		}
	}
}