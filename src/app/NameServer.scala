package app

import java.net._
import java.io._
import scala.io._

object NameServer {
	val OPERATION = Array("associar", "remover", "recuperar")
	
	val ADDR_SERVERS = Map(
		OPERATION(0) -> Array("127.0.0.1:6789"),
		OPERATION(1) -> Array("127.0.0.2:6789"),
		OPERATION(2) -> Array("127.0.0.3:6789")
	)
	
	def main(args: Array[String]): Unit = {
		ADDR_SERVERS.foreach{ case(key, value) => println(value(0)) }
		
		val addrsAssociar = ADDR_SERVERS(OPERATION(0))(0)
		
		val addr = "localhost"
		val port = 5000
		val server = createServer(addr, port)
		listenServer(server)

	}
	
	def createServer(address: String, port: Int):ServerSocket = {
		val addr = InetAddress.getByName(address)
		
		val server = new ServerSocket(port, 50, addr)
		println("Server Name running in: " + server.getInetAddress().getHostAddress + ":" + server.getLocalPort)
		
		server
	}
	
	def listenServer(server: ServerSocket) = {
		while (true) {
			val s = server.accept()
			val in = new BufferedSource(s.getInputStream()).getLines()
			val out = new PrintStream(s.getOutputStream())
		    
			val operation = in.next()
			
			println("\nServer Name Received: " + operation)
			
			var response = "12345" ////
			
			ADDR_SERVERS.foreach{ case(key, value) =>
				if(key.equalsIgnoreCase(operation))
					response = value(0)
			}
			
			out.println(response)
			out.flush()
			s.close()
		}
	}
	
}