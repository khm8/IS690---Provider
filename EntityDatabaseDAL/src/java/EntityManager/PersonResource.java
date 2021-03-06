/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package EntityManager;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import org.json.JSONArray;
import org.json.JSONObject;
import EntityDB.*;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;


/**
 * REST Web Service
 *
 * @author ramyabalaji
 */

@Path("/person/")
public class PersonResource {
    @Context
    public static UriInfo context;

    /** Creates a new instance of PersonResource */
    public PersonResource() {
    }

    /**
     * Retrieves representation of an instance of EntityDB.PersonResource
     * @return an instance of EntityDB.Person
     */
    @Path("{id}")
    @GET
    @Produces("application/json")
    public String retrievePerson(@PathParam ("id") String id) {
        //TODO return proper representation object
        JSONObject json= new JSONObject();
        try
        {
           Person person = (Person) EntityBase.selectByID(id);
           if (person==null)
                return "This Person does not exist in the system";
           json.put("Email", person.getEmail());
           json.put("Phone", person.getPhone());
           json.put("FirstName",person.getFirstName());
           json.put("LastName", person.getLastName());
           json.put("EntityId",person.getEntityId());
           return json.toString();
        } catch (Exception ex){
            return (ex.toString());           
        }                    
}

    /** POST method to update fields. Email is not updateable**/
    // Note this updated code needs to be tested - Mayank Desai
    @POST
    @Path("{id}")
    @Consumes("application/json")
    public String updatePerson(@PathParam ("id") String id, String personInfo)
    {
        try
        {
            JSONObject content=new JSONObject(personInfo);

            Person person = (Person) EntityBase.selectByID(id);
            if (!content.isNull("FirstName"))
               person.setFirstName(content.getString("FirstName"));
            if (!content.isNull("LastName"))
                person.setLastName(content.getString("LastName"));
            if (!content.isNull("Email"))
                person.setEmail(content.getString("Email"));
            if (!content.isNull("Phone"))
                person.setPhone(content.getString("Phone"));
            person.save();
            return ("Successfully Updated People- Non system User:"+ person.getEmail());
        }
        catch (Exception E)
        {
             return (E.toString());
        }
    }
   
    @Path("{id}")
    @DELETE
    public String deletePerson(@PathParam ("id") String id){
    {
      try {

          //System.out.println("ID is "+id);
          //Note: make sure this works.  Right now it is untested.
          Person person = (Person)EntityBase.selectByID(id);//getEntityByID(id, "Person");
          if(person instanceof User)
          {
               return "Error: Person is a system user.  Please delete through the User interface.";
          }

           //TODO: Create a parsible message for the application devs.
          if(person == null)
          {
              return "Error: Person doesn't exist.";
          }
          User owner=person.getOwner();
          if (owner !=null)
          {
              if (owner.getEntityId().equals(id ))//Owner is the same Entity
              {
                 person.delete(true);
              }
              else
              {
                  //Authenticate For Owner and then delete- include code for authentication
                  person.delete(true);
              }
          }
          else
          {
          person.delete(true);
          }
           return ("Successfully Deleted Person:"+ person.getEmail());
        } catch (Exception E){
           return (E.getMessage());
        }
     }
    }
 
    
    /**
     * PUT method for creating an instance of PersonResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @Path("/")
    @PUT
    @Consumes("application/json")
    public String createPerson(String json) {
      String personEmail  =null;
      try {
          JSONObject content = new JSONObject(json);
          Person person = new Person();
          person.createNewID();
          String strEmail = (String) content.get("Email");
          person.setEmail(strEmail);
          person.setFirstName((String) content.get("FirstName"));
          person.setLastName((String) content.get("LastName"));
          person.setPhone((String) content.get("Phone"));
          person.save();
          personEmail= (String) content.get("Email");
       } catch (Exception E){
             return E.toString();
       }
       return  "Successfully Added Non System User(Person): " + personEmail;
    }

    @Path ("/register/{id}")
    @PUT
    @Consumes("application/json")
    public String registerPersonAsUser(@PathParam("id") String id, String personInfo)
    {
        try
        {
           JSONObject content=new JSONObject(personInfo);
           Person p= (Person)EntityBase.selectByID(id);
           if (p==null)
               return("Cannot register this user. This Person does not exist!");
           String UserName, password, OAuthToken;
           if (content.isNull("UserName"))
               return("UserName cannot be empty");
           UserName=content.getString("UserName");
           if (content.isNull("Password"))
               return("Password cannot be empty");
           password=content.getString("Password");
           if (content.isNull("OAuthToken"))
                   OAuthToken=null;
           else
               OAuthToken=content.getString("OAUthToken");
           User registeredUser=User.createUserFromPerson(p, UserName, password, OAuthToken);
           if (registeredUser==null)
               return ("Unable to create user - Try a different user name!");
           return ("Successfully created user "+registeredUser.getUserName());

        }
        catch(Exception ex)
        {
              return (ex.toString());
        }
        
    }
    @Path("/list/all")
    @GET
    @Produces("application/json")
    public String retrieveAllPeople() {
        //TODO return proper representation object

      JSONArray jsonArray =new JSONArray();
      try {
        PersistableObject[] people = PersistableObject.getAllObjects("Person");
         for(int i = 0;i<people.length;i++)
        {
           Person p = (Person) people[i] ;
           JSONObject json = new JSONObject();
           json.put("EntityId", p.getEntityId());
           json.put("FirstName", p.getFirstName());
           json.put("LastName", p.getLastName());
           json.put("Email", p.getEmail());
           json.put("Phone", p.getPhone());
           jsonArray.put(json);
        }

        } catch (Exception ex){
            return ex.toString();
        }
        return jsonArray.toString();
    }

    @Path("/")
    @GET
    @Produces("application/json")
    public String getPeopleAsJsonArray() {
        JSONArray uriArray = new JSONArray();
        PersistableObject [] po =  PersistableObject.getAllObjects("Person");
        for (int i =0;i<po.length;i++){
            Person p = (Person) po[i];
            UriBuilder ub = context.getAbsolutePathBuilder();
            URI userUri = ub.path(p.getEntityId()).build();
            uriArray.put(userUri.toASCIIString());
        }
        return uriArray.toString();
    }

}
