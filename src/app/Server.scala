package app

import java.net._
import java.io._
import scala.io._

object Server {
	val connection = new SqliteConnection()
	
	val FUNCTIONS = Map[String, (String*)=>Any](
		"ASSOCIAR" -> associar,
		"REMOVER" -> remover,
		"RECUPERAR" -> recuperar
	)
	
	def main(args: Array[String]): Unit = {
		val addr = InetAddress.getByName("localhost")
		val port = 6789
		
		val server = new ServerSocket(port, 50, addr)
		println("Server running in: " + server.getInetAddress().getHostAddress + ":" + server.getLocalPort)
		
		while (true) {
		    val s = server.accept()
		    val in = new BufferedSource(s.getInputStream()).getLines()

		    val operation = in.next().split("#@#")
		    val typeOperation = operation(0).toUpperCase()
		    println("\nServer Received: " + typeOperation)
		    
		    val response = if( FUNCTIONS.contains(typeOperation) )
		    	FUNCTIONS(typeOperation)(operation.tail)
		    else
		    	error()

		    var out = new ObjectOutputStream(s.getOutputStream());
		    out.writeObject(response)
		    s.close()
		}
	}
	
	def associar(value: String*) = {
		if(connection.insert(value(0), (value(1), value(2), value(3).toDouble)))
			"added successfully"
		else
			null
	}
	
	def remover(value: String*) = {
		if(connection.delete(value(0)))
			"removed successfully"
		else
			null
	}
	
	def recuperar(value: String*): Array[(String, String, Double)] = {
		val tickets = connection.selectAllTicket(value(0))

		if(tickets.length == 0)
			null
		else
			tickets
	}
	
	
	def error() = {
		println("error")
		"ERROR"
	}
}