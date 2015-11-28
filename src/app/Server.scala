package app

import java.net._
import java.io._
import scala.io._

object Server {
	val separator = "#@#"
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

		    val request = in.next()
		    
		    val operation = request.split(separator)
		    val typeOperation = operation(0).toUpperCase()
		    println("\nServer Received: " + typeOperation)
		    
		    if(typeOperation != "RECUPERAR")
		    	replicatesToAll(request)
		    
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
	
	
	//replicates operation to all servers
	def replicatesToAll(command:String) = {
		val addrServer = "localhost"
		val addrPort = 5000
		
		try{
			val s = new Socket(InetAddress.getByName(addrServer), addrPort)
			lazy val in = new BufferedSource(s.getInputStream()).getLines()
			val out = new PrintStream(s.getOutputStream())
			
			out.println("GET_ALL_SERVERS")
			out.flush()
			
			val request = in.next()
			s.close()
			
			if(request != ""){
				for(addresses <- request.split(separator)){
					val (addrOperation, port) = (addresses.split(":")(0), addresses.split(":")(1))
					sendCommand(addrOperation, port.toInt, command)
				}
			}
			else
				"Server Naee not respond"
		}
		catch{
			case e:ConnectException => "Server Name Error"
		}
	}
	
	//send command to other server
	def sendCommand(addr:String, port:Int, command:String) = {
		val s = new Socket(InetAddress.getByName(addr), port)
		val out = new PrintStream(s.getOutputStream())
		
		out.println(command)
		out.flush()
		
		val in = new ObjectInputStream(s.getInputStream());
		val obj = in.readObject()
		
		s.close()
		
		obj
	}
}