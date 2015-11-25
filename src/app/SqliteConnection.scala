package app

import java.sql.DriverManager
import java.sql.Connection

object SqliteConnection {
	val conn = DriverManager.getConnection("jdbc:sqlite:dataBase/Traffic_Ticket.db")
	
	val TABLE_LICENSE_PLATE = "CREATE TABLE IF NOT EXISTS license_plate("+
								"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
								"license_plate TEXT NOT NULL UNIQUE"+
							");";
	val TABLE_TRAFFIC_TICKET = "CREATE TABLE IF NOT EXISTS traffic_ticket("+
								"id_traffic_ticket INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
								"description TEXT NOT NULL,"+
								"date REAL NOT NULL,"+
								"value REAL NOT NULL,"+
								"id_license_plate INTEGER NOT NULL,"+
								"FOREIGN KEY(id_license_plate) REFERENCES license_plate(id)"+
							");"
						
	val INSERT_LICENSE_PLATE = "INSERT OR IGNORE INTO license_plate(license_plate) VALUES (?)"
	val INSERT_TRAFFIC_TICKET = "INSERT INTO traffic_ticket(description, date, value, id_license_plate) VALUES (?,?,?,?)"
	val SELECT_LICENSE_PLATE = "SELECT * FROM license_plate WHERE license_plate = (?)"
	val SELECT_TRAFFIC_TICKET = "SELECT * FROM traffic_ticket LEFT OUTER JOIN license_plate ON traffic_ticket.id_license_plate = license_plate.id WHERE license_plate.license_plate = (?)"
	
	/*def main(args: Array[String]): Unit = {
		Array(TABLE_LICENSE_PLATE, TABLE_TRAFFIC_TICKET).foreach {
			conn.prepareStatement(_).executeUpdate()
		}
	}*/
	
	def insert(license_plate:String, infos:(String, Double, Double)) = {
		var stmt = conn.prepareStatement(INSERT_LICENSE_PLATE)
		stmt.setString(1, license_plate)
		stmt.executeUpdate()
		
		stmt = conn.prepareStatement(INSERT_TRAFFIC_TICKET)
		stmt.setString(1, infos._1)
		stmt.setDouble(2, infos._2)
		stmt.setDouble(3, infos._3)
		stmt.setDouble(4, selectLicense(license_plate)._1)
		
		stmt.executeUpdate()
		stmt.close()
	}
	
	
	def selectLicense(license_plate:String):(Double, String) = {
		val stmt = conn.prepareStatement(SELECT_LICENSE_PLATE)
		
		stmt.setString(1, license_plate)
		val result = stmt.executeQuery()
		
		var id = 0.0
		var value = ""
		while(result.next()){
			id = result.getDouble("id")
			value = result.getString("license_plate")
		}
		
		(id, value)
	}
	
	
	def selectAllTicket(license_plate:String):(String, Double, Double) = {
		val stmt = conn.prepareStatement(SELECT_TRAFFIC_TICKET)
		
		stmt.setString(1, license_plate)
		val result = stmt.executeQuery()
		
		var description = ""
		var date = 0.0
		var value = 0.0
		while(result.next()){
			description = result.getString("description")
			date = result.getDouble("date")
			value = result.getDouble("value")
			
			println(result.getString("description"))
			println(result.getDouble("date"))
			println(result.getDouble("value"))
		}
		stmt.close()
		
		(description, date, value)
	}
}