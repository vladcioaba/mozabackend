
import org.apache.commons.dbcp2.BasicDataSource;

import com.mozaicgames.backend.CBackendServer;
import com.mozaicgames.backend.CHandlerRegisterUserAnonymous;
import com.mozaicgames.backend.CHandlerRoot;

public class Main 
{
	
	public static void main(String[] args) 
	{
		int port = 8081;
		//validating the Java Arguments
        if (args.length > 0) 
        {
            try 
            {
                if (args.length == 2) 
                {
                    if (args[0].equals("-p")) 
                    {
                    	port = Integer.parseInt(args[1]);
                    } 
                    else 
                    {
                        throw new Exception("Invalid argument '" + args[0] + "'.");
                    }
                } 
                else 
                {
                    throw new Exception("Invalid number of arguments.");
                }
            } 
            catch (Exception e) 
            {
                System.err.println("Error with the arguments.");
                System.err.println(e.getMessage());
                System.err.println("java -jar mozabackendserver.jar [-p portNumber]");
                return;
            }
        }
        
        BasicDataSource ds = null;
        
        try 
        {
        	ds = new BasicDataSource();
        }
        catch (Exception e)
        {
        	System.err.println(e.getMessage());
            return;
        }
        
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost:3306/mozaic");
        ds.setUsername("dev");
        ds.setPassword("Mozaic123!");
        
        final String minClientVersionAllowed = "1.0.0";
        final String encriptionCode = "mozadev123";
    	CBackendServer backendServer = new CBackendServer();
		try 
        {
			backendServer.registerHandler("", new CHandlerRoot(ds));
			backendServer.registerHandler("register_user_anonymous", new CHandlerRegisterUserAnonymous(ds, encriptionCode, minClientVersionAllowed));
        }
		catch (Exception e)
		{
			System.err.println("Register handler Null pointer exception: " + e.getMessage());
            return;
		}
		
		backendServer.startOnPort(port);
	}

}
