package app

import java.net._
import java.io._
import scala.io._
import scala.swing._
import java.awt.GridBagConstraints

class UI(val funcConnect: (String) => Object, val separator:String){
	private val comboBox = new ComboBox(List("Associar", "Remover", "Recuperar"))
	private val fieldLicense = new TextField { columns = 15 }
	private val fieldDate = new TextField { columns = 15 }
	private val fieldValue = new TextField { columns = 10 }
	private val textDescription = new TextArea(5,30)
	private val textResponse = new TextArea(10,30)
	
	private var response = new Object()
	
	
	def createRequest():String = {
    	var command:String = ""
    	val value = comboBox.item.toUpperCase()
    	
    	if(fieldLicense.text != ""){
	    	if(value == "ASSOCIAR"){
	    		if(!(comboBox.item == "" || textDescription.text == "" || fieldDate.text == "" || fieldValue.text == "")){
		    		command += comboBox.item + separator
		    		command += fieldLicense.text + separator
		    		command += textDescription.text + separator
		    		command += fieldDate.text + separator
		    		command += fieldValue.text
	    		}
	    	}
	    	else if(value == "REMOVER" || value == "RECUPERAR"){
	    		command += comboBox.item + separator
	    		command += fieldLicense.text
	    	}
    	}
    	command
    }
	
	
	def printResponse(response:Object):String = {
		var strObj = ""
		if(response.isInstanceOf[Array[(String, String, Double)]]){
			val obj = response.asInstanceOf[Array[(String, String, Double)]]
			
			for(o <- obj){
				strObj +=
					"Description: " + o._1 + "\n" +
					"Date: " + o._2 + "\n" +
					"Value: " + o._3 +"$" + "\n\n"
			}
		}
		else if (response.isInstanceOf[String]){
			val obj = response.asInstanceOf[String]
			strObj = obj
		}
		else{
			strObj = "License plate not exist."
		}
		textResponse.text = strObj 
		""
	}
	

	//panelSouth
	private val panelSouth = new FlowPanel{
		val btnEnter = Button("Enter") {}
		contents += btnEnter
		contents += Button("Close") {sys.exit(0)}
		border = Swing.EtchedBorder(Swing.Lowered)
		
		listenTo(btnEnter)
		reactions += {
			case event.ButtonClicked(this.btnEnter) => {
				val request:String = createRequest()
				var response = new Object()
				if(request != ""){
					response = funcConnect(request)
					printResponse(response)
				}
				else
					response = "Complete all fields"
			}
		}
	}//panelSouth
	
	//panelCenter
	val panelCenter = new GridBagPanel {
	    def constraints(x: Int, y: Int, 
			    gridwidth: Int = 1, gridheight: Int = 1,
			    weightx: Double = 0.0, weighty: Double = 0.0,
			    fill: GridBagPanel.Fill.Value = GridBagPanel.Fill.None,
			    anchor: GridBagPanel.Anchor.Value = GridBagPanel.Anchor.FirstLineStart) 
	    : Constraints = {
	      val c = new Constraints
	      c.gridx = x
	      c.gridy = y
	      c.gridwidth = gridwidth
	      c.gridheight = gridheight
	      c.weightx = weightx
	      c.weighty = weighty
	      c.fill = fill
	      c.anchor = anchor
	      c
	    }
	    
	    add(new Label("Operation: ") {}, constraints(1, 0, gridwidth=2, fill=GridBagPanel.Fill.Both, anchor=GridBagPanel.Anchor.LineEnd))
	    add(comboBox, constraints(2, 0, gridwidth=0, anchor=GridBagPanel.Anchor.LineEnd))
	    
	    add(new Label("License Plate: ") {}, constraints(0, 1, anchor=GridBagPanel.Anchor.LastLineEnd))
	    add(fieldLicense, constraints(1, 1, gridwidth=2))
	    
	    add(new Label("Date: ") {}, constraints(0, 2, anchor=GridBagPanel.Anchor.LineEnd))
	    add(fieldDate, constraints(1, 2, gridwidth=2))
	    
	    add(new Label("Value: ") {}, constraints(0, 3, anchor=GridBagPanel.Anchor.FirstLineEnd))
	    add(fieldValue, constraints(1, 3, gridwidth=2))
	    
	    add(new Label("Description: ") {}, constraints(0, 4, anchor=GridBagPanel.Anchor.FirstLineEnd))
	    add(new ScrollPane(textDescription), constraints(1, 4, gridwidth=2))
	    
	    add(new Label("Response: ") {}, constraints(0, 5, anchor=GridBagPanel.Anchor.FirstLineEnd))
	    add(new ScrollPane(textResponse), constraints(1, 5, weighty = 0.0, gridwidth=2))
	    
	    listenTo(comboBox.selection)
	    reactions += {
	    	case event.SelectionChanged(_) => hiddenFields(comboBox.item)
	    }
	    
	    def hiddenFields(item: String) = {
	    	val value = item.toUpperCase()
	    	if(value == "ASSOCIAR"){
	    		fieldDate.editable = true
	    		fieldValue.editable = true
	    		textDescription.editable = true
	    	}
	    	else if(value == "REMOVER" || value == "RECUPERAR"){
	    		fieldDate.editable = false
	    		fieldDate.text = ""
	    		fieldValue.editable = false
	    		fieldValue.text = ""
	    		textDescription.editable = false
	    		textDescription.text = ""
			}
		}
	}//panelCenter

	
	val frame = new MainFrame{
		title = "Traffic Ticket Control"
		preferredSize  = new Dimension(500,450)
		minimumSize = new Dimension(500,450)
		
		contents = new BorderPanel {
			add(panelCenter, BorderPanel.Position.Center)
			add(panelSouth, BorderPanel.Position.South)
		}
		centerOnScreen
	}
}

object Client {
	val nameFileKey = "key.txt"
	val separator = "#@#"
	val addrServer = "localhost" //address Server Name
	val addrPort = 5000
	
	def main(args: Array[String]): Unit = {
		val ui = new UI(connectServer, separator)
    	ui.frame.visible = true
	}
	
	def connectServer(command: String) = {
		try{
			val s = new Socket(InetAddress.getByName(addrServer), addrPort)
			lazy val in = new ObjectInputStream(s.getInputStream())
			
			//ncrypt command
			val commandBytes = EncryptAES.encryption(command.split(separator)(0), EncryptAES.readKeyInFile(nameFileKey))

			val out = new ObjectOutputStream(s.getOutputStream())			
			out.writeObject(commandBytes)
			out.flush()
			
			val objResponse = in.readObject()
			
			s.close()
			
			//decrypt response
			var response = ""
			if(objResponse.isInstanceOf[Array[Byte]]){
				response = EncryptAES.decryption(objResponse.asInstanceOf[Array[Byte]], EncryptAES.readKeyInFile(nameFileKey))
				response = response.toUpperCase()
			}

			println("\nClient Received: " + response)
			if(response != ""){
				val (addrOperation, port) = (response.split(":")(0), response.split(":")(1))
				
				sendCommand(addrOperation, port.toInt, command)
			}
			else
				"Operation not exist"
		}
		catch{
			case e:ConnectException => "Server Error"
		}
	}
	
	//send command to operations server
	def sendCommand(addr:String, port:Int, command:String) = {
		val s = new Socket(InetAddress.getByName(addr), port)
		val out = new ObjectOutputStream(s.getOutputStream())
		
		//encrypt command
		val commandBytes = EncryptAES.encryption(command, EncryptAES.readKeyInFile(nameFileKey))
		out.writeObject(commandBytes)
		out.flush()
		
		val in = new ObjectInputStream(s.getInputStream());
		val obj = in.readObject()
		
		s.close()
		
		obj
	}
}