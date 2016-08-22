package ca.uqam.latece.rest.garden;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import ca.uqam.latece.rest.authentication.Authentication;
import ca.uqam.latece.rest.database.Database;

@Path("/v1/gardenservice")
public class GardenService {
	Connection c = Database.getConnection();
	
	@Path("/users")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.TEXT_HTML)
	public Response postUsers(@FormParam("token") String token, @FormParam("date") String date) throws SQLException{
		ResultSet resultSet = Database.tableRequest("SELECT COUNT(*) FROM User WHERE token ='" + token + "'");
		if(resultSet.next()){
			Database.operationOnTable("UPDATE User SET token_expiration_date=date('" + date + "') WHERE token='" + token + "'");
		}else{
			String sql = "INSERT INTO user(token, token_expiration_date) VALUES('" + token + "', date('" + date+ "'))";
			Database.operationOnTable(sql);
		}
		return Response.status(200).entity("The user has been added.").build();
	}
	
	@SuppressWarnings("unchecked")
	@Path("/gardens/")
	@GET
	@Produces("application/json")
	public String gardens(@QueryParam("access_token") String accessToken) throws ClassNotFoundException, SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ParseException{
		JSONArray gardensList = new JSONArray();
		JSONObject gardens = new JSONObject();
		
		if(Authentication.authenticationByToken(accessToken)){
			ResultSet resultSet = Database.tableRequest("SELECT name, id FROM Garden");
			
			if(!resultSet.next()){
				gardens.put("error", "no data found");
			}else{
				resultSet.beforeFirst();
				
				while(resultSet.next()){
					JSONObject gardenAttributes = new JSONObject();
					gardenAttributes.put("id", resultSet.getInt("id"));
					gardenAttributes.put("name", resultSet.getString("name"));
					gardensList.add(gardenAttributes);
					gardenAttributes = null;
				}
				gardens.put("gardens", gardensList);
			}
		}else{
			gardens.put("error", "Access denied: The access token is invalid.");
		}
		return gardens.toJSONString();
	}

	@Path("/gardens/")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.TEXT_HTML)
	public Response postGardens(@QueryParam("access_token") String accessToken, @FormParam("name") String name, @FormParam("category") int category, @FormParam("address") String address ) throws SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ParseException{
		int responseCode = 404;
		String responseMessage = "The garden was not created.";
		
		if(Authentication.authenticationByToken(accessToken)){
			JSONObject decryptedToken = Authentication.getTokenDecrypted(accessToken);

			if((int)(long)decryptedToken.get("role") == 2){
				Pattern namePattern = Pattern.compile("[A-Za-z0-9_\\s]+");
				Pattern addressPattern = Pattern.compile("[0-9]+\\s[A-Za-z0-9_\\s]+");
				
				if(name != null && name != "" && namePattern.matcher(name).matches()){
					String sql = "SELECT COUNT(*) AS exist FROM garden WHERE name='" + name + "'";
					ResultSet resultSet = Database.tableRequest(sql);
					resultSet.next();
					
					if(resultSet.getInt("exist") == 0){
						sql = "SELECT id FROM gardenCategory";
						resultSet = Database.tableRequest(sql);
						boolean exist = false;
						
						while(resultSet.next() && exist == false){
							int x = resultSet.getInt("id");
							
							if(x == category){
								exist = true;
							}
						}
						
						if(exist){
							if(address != null && address != "" && addressPattern.matcher(address).matches()){
								sql = "INSERT INTO garden(name, ID_category, address) VALUES('" + name + "', '" + category + "', '" + address + "')";
								Database.operationOnTable(sql);
								
								responseCode = 200;
								responseMessage = "The garden has been created.";
							}else{
								responseCode = 200;
								responseMessage = "The address is not valid.";
							}
						}else{
							responseCode = 200;
							responseMessage = "The category is not valid.";
						}
					}else{
						responseCode = 200;
						responseMessage = "A garden with this name already exist.";
					}
				}else{
					responseCode = 200;
					responseMessage = "The name is not valid.";
				}
			}else{
				responseCode = 401;
				responseMessage = "Access denied: you are not authorized to add a garden.";
			}
		}else{
			responseCode = 401;
			responseMessage = "Access denied: the access token is invalid.";
		}
		return Response.status(responseCode).entity(responseMessage).build();
	}
	
	@SuppressWarnings("unchecked")
	@Path("/gardens/{id:[0-9]*}")
	@GET
	@Produces("application/json")
	public String gardenInfo(@QueryParam("access_token") String accessToken, @PathParam("id") int id) throws ClassNotFoundException, SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ParseException{	
		JSONObject garden = new JSONObject();

		ResultSet resultSet = Database.tableRequest("SELECT G.name, GC.name, address, G.id FROM Garden AS G, GardenCategory AS GC WHERE ID_Category = GC.id AND G.id =" + id);
		
		if (Authentication.authenticationByToken(accessToken)) {
			if(!resultSet.next()){
				garden.put("error", "no data found.");
			}else{
				garden.put("id", id);
				garden.put("name", resultSet.getString("G.name"));
				garden.put("category", resultSet.getString("GC.name"));
				garden.put("address", resultSet.getString("address"));
			}
			}else{
				garden.put("error", "Access denied: You are not authorized to view garden.");
			}
		return garden.toJSONString();
	}
	
	@Path("/gardens/{id:[0-9]*}")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.TEXT_HTML)
	public Response updateGarden(@QueryParam("access_token") String accessToken,@PathParam("id") String id, @FormParam("name") String name, @FormParam("category") int category, @FormParam("address") String address) throws SQLException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ParseException{
		int responseCode = 404;
		String responseMessage = "Garden with the id " + id + " is not in the Database.";
		
		if (Authentication.authenticationByToken(accessToken)) {
			JSONObject decryptedToken = Authentication.getTokenDecrypted(accessToken);
			
			if((int)(long)decryptedToken.get("role") == 2){
				Pattern namePattern = Pattern.compile("[A-Za-z0-9_\\s]+");
				Pattern addressPattern = Pattern.compile("[A-Za-z0-9_\\s]+");
				
				if(name != null && name != "" && namePattern.matcher(name).matches()){
					String sql = "SELECT id FROM gardenCategory";
					ResultSet resultSet = Database.tableRequest(sql);
					boolean exist = false;
					
					while(resultSet.next() && exist == false){					
						if(resultSet.getInt("id") == category){
							exist = true;
						}
					}
					if(exist){
						if(address != null && address != "" && addressPattern.matcher(address).matches()){
							sql = "UPDATE garden SET name='" + name + "', ID_category=" + category + ", address='" + address + "' WHERE id=" + id;
							Database.operationOnTable(sql);
							
							responseCode = 200;
							responseMessage = "The garden has been updated.";
						}else{
							responseMessage = "The address is not valid.";
						}
					}else{
						responseMessage = "The category is not valid.";
					}
				}else{
					responseMessage = "The name is not valid.";
				}
			}else{
				responseMessage = "Access denied: you are not authorized to modify a garden.";
			}
		} else {
			responseCode = 401;
			responseMessage = "Access denied: the access token is invalid.";
		}
		return Response.status(responseCode).entity(responseMessage).build();
	}
	
	@Path("/gardens/{id:[0-9]*}")
	@DELETE
	@Produces(MediaType.TEXT_HTML)
	public Response deleteGarden(@QueryParam("access_token") String accessToken, @PathParam("id") String id) throws ClassNotFoundException, SQLException{
		int responseCode = 404;
		String responseMessage = "Garden with the id " + id + " is not in the Database.";
		
		if (Authentication.authenticationByToken(accessToken)){
			ResultSet resultSet = Database.tableRequest("SELECT COUNT(*) FROM Garden WHERE ID =" + id);
			if(resultSet.next()){
				Database.operationOnTable("DELETE FROM Garden WHERE id = " + id);
				
				responseCode = 200;
				responseMessage = "The garden has been deleted.";
			}
		} else {
			responseCode = 401;
			responseMessage = "Access denied: the access token is invalid.";
		}
		return Response.status(responseCode).entity(responseMessage).build();
	}
	
	@SuppressWarnings("unchecked")
	@Path("/categories/")
	@GET
	@Produces("application/json")
	public String categories(@QueryParam("access_token") String accessToken) throws ClassNotFoundException, SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ParseException{
		JSONArray categoriesArray = new JSONArray();
		JSONObject category = new JSONObject();
		JSONObject categories = new JSONObject();
		
		if(Authentication.authenticationByToken(accessToken)){
			ResultSet resultSet = Database.tableRequest("SELECT id, name FROM GardenCategory");
			
			if(!resultSet.next()){
				category.put("error", "no data found");
			}else{
				resultSet.beforeFirst();
				
				while(resultSet.next()){
					category.put("id", resultSet.getInt("id"));
					category.put("name", resultSet.getString("name"));
					
					categoriesArray.add(category);
					category = new JSONObject();
				}
				categories.put("categories", categoriesArray);
			}
		}else{
			categories.put("error", "Access denied: The access token is invalid.");
		}
		return categories.toJSONString();
	}
}