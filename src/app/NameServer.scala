package app

import java.net._
import java.io._
import scala.io._

object NameServer {
	val OPERATION = Array("ASSOCIAR", "REMOVER", "RECUPERAR")
	
	val ADDR_SERVERS = Map(
		OPERATION(0) -> Array("127.0.0.1:6789","127.0.0.1:6789","127.0.0.1:6789","127.0.0.1:6789"),
		OPERATION(1) -> Array("127.0.0.1:6789"),
		OPERATION(2) -> Array("127.0.0.1:6789","127.0.0.1:6789","127.0.0.1:6789")
	)
	
	//last server used of each operation
	var BALANCING_SERVERS = collection.mutable.Map(
		OPERATION(0) -> 0,
		OPERATION(1) -> 0,
		OPERATION(2) -> 0
	)
	
	def main(args: Array[String]): Unit = {
		//ADDR_SERVERS.foreach{ case(key, value) => println(value(0)) }
		
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
			
			var response = ""

			ADDR_SERVERS.foreach{ case(key, value) =>
				if(key.equalsIgnoreCase(operation))
					response = value(loadBalancing(operation))
			}
			
			out.println(response)
			out.flush()
			s.close()
		}
	}
	
	//Get next server in array
	def loadBalancing(operation:String):Int = {
		var response = 0
		
		val oper = operation.toUpperCase()
		if(BALANCING_SERVERS.contains(oper)){
			response = BALANCING_SERVERS(oper)
			if(BALANCING_SERVERS(oper) < ADDR_SERVERS(oper).length -1)
				BALANCING_SERVERS(oper) += 1
			else
				BALANCING_SERVERS(oper) = 0
		}
		response
	}
	
}