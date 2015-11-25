package app

import java.net._
import java.io._
import scala.io._

object Server {
	val FUNCTIONS = Map[String, (String*)=>String](
		"associar" -> associar,
		"remover" -> remover,
		"recuperar" -> recuperar
	)
	
	def main(args: Array[String]): Unit = {
		val addr = InetAddress.getByName("localhost")
		val port = 6789
		
		val server = new ServerSocket(port, 50, addr)
		println("Server running in: " + server.getInetAddress().getHostAddress + ":" + server.getLocalPort)
		
		while (true) {
		    val s = server.accept()
		    val in = new BufferedSource(s.getInputStream()).getLines()
		    val out = new PrintStream(s.getOutputStream())
		    
		    val operation = in.next().split(",")
		    println("\nServer Received: " + operation(0))
		    
		    
		    val response = if( FUNCTIONS.contains(operation(0)) ){
		    	FUNCTIONS(operation(0))(operation.tail)
		    }
		    else{
		    	error()
		    }
		    
		    out.println(response)
		    out.flush()
		    s.close()
		}
	}
	
	def associar(value: String*) = {
		println("associar")
		"associar"
	}
	
	def remover(value: String*) = {
		println("remover")
		"remover"
	}
	
	def recuperar(value: String*) = {
		println("recuperar")
		"recuperar"
	}
	
	
	def error() = {
		println("error")
		"ERROR"
	}
}