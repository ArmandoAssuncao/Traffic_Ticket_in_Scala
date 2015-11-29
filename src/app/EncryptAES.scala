package app

import javax.crypto._
import javax.crypto.spec.SecretKeySpec

object EncryptAES{
	private val KeyGen = KeyGenerator.getInstance("AES")
	KeyGen.init(128);
	private val AesCipher = Cipher.getInstance("AES");
	
	def genetateKey():SecretKey = {
		KeyGen.generateKey()
	}
	
	def encryption(text:String, generatedKey:SecretKey):Array[Byte] = {
		AesCipher.init(Cipher.ENCRYPT_MODE, generatedKey);
		var byteCipherText = AesCipher.doFinal(text.getBytes);
		byteCipherText
	}
	def encryption(obj:Object, generatedKey:SecretKey):Array[Byte] = {
		AesCipher.init(Cipher.ENCRYPT_MODE, generatedKey);
		var byteCipherObj = AesCipher.doFinal(serialize(obj));
		byteCipherObj
	}
	
	def decryption(arrayBytes:Array[Byte], generatedKey:SecretKey):String = {
		AesCipher.init(Cipher.DECRYPT_MODE, generatedKey);
		var bytePlainText = AesCipher.doFinal(arrayBytes)
		var text = new String(bytePlainText)
		text
	}
	def decryptionObject(arrayBytes:Array[Byte], generatedKey:SecretKey):Object = {
		AesCipher.init(Cipher.DECRYPT_MODE, generatedKey);
		var byteObj = AesCipher.doFinal(arrayBytes)
		val obj = deserialize(byteObj)
		obj
	}
	
	
	def writeKeyToFile(nameFile:String, generatedKey:SecretKey):Boolean = {
		val fos = new java.io.FileOutputStream(new java.io.File(nameFile));
		val bos = new java.io.BufferedOutputStream(fos);
		bos.write(generatedKey.getEncoded);
		bos.close();
		
		true
	}
	
	def readKeyInFile(nameFile:String):SecretKey = {
		val fis = new java.io.FileInputStream(new java.io.File(nameFile));
		
		var value = -1
		var arrayBytes = collection.mutable.ArrayBuffer[Byte]()
		do{
			value = fis.read()
			if(value != -1)
				arrayBytes+= value.toByte
		}while(value != -1)
		
		fis.close();
		
		new SecretKeySpec(arrayBytes.toArray, "AES")
	}
	
	def serialize(obj:Object):Array[Byte] = {
        val b = new java.io.ByteArrayOutputStream()
        b.toByteArray();
    }
	
	private def deserialize(bytes:Array[Byte]):Object = {
        val b = new java.io.ByteArrayInputStream(bytes);
        val o = new java.io.ObjectInputStream(b);
        o.readObject();
    }
}