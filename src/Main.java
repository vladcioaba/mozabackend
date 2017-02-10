

import com.mozaicgames.backend.CBackendDatabaseAutentificationData;
import com.mozaicgames.backend.CBackendServer;
import com.mozaicgames.backend.CHandlerRegisterDevice;

public class Main {

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
		
        String url = "jdbc:mysql://localhost:3306/mozaic";
        String user = "dev";
        String password = "Mozaic123!";

        CBackendDatabaseAutentificationData autentificationData = new CBackendDatabaseAutentificationData(url, user, password);
        
		CBackendServer backendServer = new CBackendServer();
		
		backendServer.registerHandler("register_device", new CHandlerRegisterDevice(autentificationData));
		
		backendServer.startOnPort(port);
	}

}
