package app

import java.net._
import java.io._
import scala.io._

object NameServer {
	val nameFileKey = "key.txt"
	val separator = "#@#"
	val OPERATION = Array("ASSOCIAR", "REMOVER", "RECUPERAR")
	
	val ADDR_SERVERS = Map(
		OPERATION(0) -> Set("127.0.0.1:6789","127.0.0.1:6789","127.0.0.1:6789","127.0.0.1:6789"),
		OPERATION(1) -> Set("127.0.0.1:6789"),
		OPERATION(2) -> Set("127.0.0.1:6789","127.0.0.1:6789","127.0.0.1:6789")
	)
	
	//last server used of each operation
	var BALANCING_SERVERS = collection.mutable.Map(
		OPERATION(0) -> 0,
		OPERATION(1) -> 0,
		OPERATION(2) -> 0
	)
	
	def main(args: Array[String]): Unit = {
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
			val in = new ObjectInputStream(s.getInputStream())
			val out = new ObjectOutputStream(s.getOutputStream())
		    
			val objOperation = in.readObject()
			
			//decrypt command
			var operation = ""
			if(objOperation.isInstanceOf[Array[Byte]]){
				operation = EncryptAES.decryption(objOperation.asInstanceOf[Array[Byte]], EncryptAES.readKeyInFile(nameFileKey))
				operation = operation.toUpperCase()
			}
			
			var response = ""
			//return all operations servers
			if(operation == "GET_ALL_SERVERS"){
				ADDR_SERVERS.foreach{ case(key, value) =>
					value.foreach {
						response += _ + separator
					}
				}
			}
			else{
				if(ADDR_SERVERS.contains(operation)){
					val addresses = ADDR_SERVERS(operation).toList
					var online = false
					var count = 0
					while(!online && count < addresses.size){
						response = addresses(loadBalancing(operation))
						online = isServerOnline(response)
						count +=1
					}
				}
			}
			
			println("\nServer Name Received: " + operation)
			//println("\nServer Name Respond: " + response)
			
			//encrypt response
			val responseBytes = EncryptAES.encryption(response, EncryptAES.readKeyInFile(nameFileKey))
			
			out.writeObject(responseBytes)
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
			if(BALANCING_SERVERS(oper) < ADDR_SERVERS(oper).size -1)
				BALANCING_SERVERS(oper) += 1
			else
				BALANCING_SERVERS(oper) = 0
		}
		response
	}
	

	def isServerOnline(address: String):Boolean = {
		val (addrServer, addrPort) = (address.split(":")(0), address.split(":")(1))

		try {
			val s = new Socket(InetAddress.getByName(addrServer), addrPort.toInt)
			s.close()
	        true
	    }
		catch {
	        case e:ConnectException => false
	    }
	}
	
}