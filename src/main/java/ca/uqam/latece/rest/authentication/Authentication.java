package ca.uqam.latece.rest.authentication;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ca.uqam.latece.rest.database.Database;

@Path("/authentication")
public class Authentication {
	
	private static JSONObject decryptToken(String token) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, org.json.simple.parser.ParseException{
		JSONObject infos = null;
		String secret = "S3cre7KeyTh4tUs3";
		Key secretKey = new SecretKeySpec(secret.getBytes(), "AES");
		Cipher c = Cipher.getInstance("AES");
		
		c.init(Cipher.DECRYPT_MODE, secretKey);
		
		byte []enc = Base64.getUrlDecoder().decode(token);
		byte[] decoded = c.doFinal(enc);
		String decrypted = new String(decoded);
		JSONParser jp = new JSONParser();
		
		infos = (JSONObject) jp.parse(decrypted);
		
		return infos;
	}
	
	public static boolean authenticationByToken(String token) throws SQLException{
		boolean toReturn = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String today = sdf.format(new Date());
	
		if(token != null){
			ResultSet resultSet = Database.tableRequest("SELECT COUNT(*) AS exist FROM user WHERE token_expiration_date > date('" + today + "') AND token='" + token + "'");
			resultSet.next();
			
			if(resultSet.getInt("exist") > 0){
				toReturn = true;
			}
		}
		return toReturn;
	}
	
	public static JSONObject getTokenDecrypted(String token) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, org.json.simple.parser.ParseException{
		return decryptToken(token);
	}
	
	private static String encryptPassword(String password) throws NoSuchAlgorithmException{		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(password.getBytes());
		
		return Base64.getUrlEncoder().encodeToString(md.digest());
	}
	
	public static String getEncryptedPassword(String password) throws NoSuchAlgorithmException{
		return encryptPassword(password);
	}
}
