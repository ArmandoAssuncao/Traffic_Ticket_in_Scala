package app

import java.net._
import java.io._
import scala.io._

object Server {
	val addr = InetAddress.getByName("localhost")
	val port = 6789
	
	val connection = new SqliteConnection()
	val separator = "#@#"
	
	val FUNCTIONS = Map[String, (String*)=>Object](
		"ASSOCIAR" -> associar,
		"REMOVER" -> remover,
		"RECUPERAR" -> recuperar
	)
	
	def main(args: Array[String]): Unit = {
		val server = new ServerSocket(port, 50, addr)
		println("Server running in: " + server.getInetAddress().getHostAddress + ":" + server.getLocalPort)
		
		while (true) {
		    val s = server.accept()
		    val in = new BufferedSource(s.getInputStream()).getLines()

		    if(!in.isEmpty){
			    val request = in.next()
			    var response = new Object()
			    
			    val operation = request.split(separator)
			    val typeOperation = operation.head.toUpperCase()
			    println("\nServer Received: " + typeOperation)
			    
			    //exec when command not is replicate
			    if(operation.head != "REPLICATE"){
				    response = if( FUNCTIONS.contains(typeOperation) )
				    	FUNCTIONS(typeOperation)(operation.tail)
				    else
				    	error()
				    	
				    if(typeOperation !="RECUPERAR"){
				    	replicatesToAll(request)
				    }
			    }
			    else{
			    	var operReply = request.split(separator).tail
			    	var typeOperReply = operReply.head.toUpperCase()
			    	
			    	response = if( FUNCTIONS.contains(typeOperReply) )
				    	FUNCTIONS(typeOperReply)(operReply.tail)
				    else
				    	error()
			    }
	
			    var out = new ObjectOutputStream(s.getOutputStream());
			    out.writeObject(response)
		    }
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
		println("operation not found")
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
				var arrayBuffer = request.split(separator).toSet.to[collection.mutable.ArrayBuffer]
				val myAddress = addr.getHostAddress+":"+port
				arrayBuffer -= myAddress
				
				for(address <- arrayBuffer){
					println("address: " + address)
					val (addrOperation, port) = (address.split(":")(0), address.split(":")(1))
					sendCommand(addrOperation, port.toInt, "REPLICATE" + separator + command)
				}
			}
			else
				"Server Name not respond"
		}
		catch{
			case e:ConnectException => "Server Name Error"
		}
	}
	
	//send command to other server
	def sendCommand(addr:String, port:Int, command:String) = {
		var obj = new Object()
		try{
			val s = new Socket(InetAddress.getByName(addr), port)
			val out = new PrintStream(s.getOutputStream())
			
			out.println(command)
			out.flush()
			
			val in = new ObjectInputStream(s.getInputStream());
			obj = in.readObject()
			
			s.close()
		}
		catch{
			case e:ConnectException => "Server Error"
		}
		
		obj
	}
}