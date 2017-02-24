
import org.apache.commons.dbcp2.BasicDataSource;

import com.mozaicgames.core.CBackendServer;
import com.mozaicgames.core.CHandlerRoot;
import com.mozaicgames.handlers.CHandlerRegisterDevice;
import com.mozaicgames.handlers.CHandlerRegisterGameData;
import com.mozaicgames.handlers.CHandlerRegisterUserAnonymous;
import com.mozaicgames.handlers.CHandlerUpdateSession;
import com.mozaicgames.handlers.CHandlerRegisterSession;
import com.mozaicgames.utils.CBackendSessionCleanerScheduler;
import com.mozaicgames.utils.CBackendSessionManager;

public class Main 
{
	
	private static CBackendSessionManager 							mSessionManager = null;
	private static CBackendSessionCleanerScheduler					mSessionManagerCleaner = null;
	
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
        
        final String encriptionCode = "mozadev123";
    	BasicDataSource ds = null;
        try 
        {
        	ds = new BasicDataSource();
        	mSessionManager = new CBackendSessionManager(ds, encriptionCode);
        	mSessionManagerCleaner = new CBackendSessionCleanerScheduler(mSessionManager);
        	mSessionManagerCleaner.startService();
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
        CBackendServer backendServer = new CBackendServer();
		try 
        {
			backendServer.registerHandler("", new CHandlerRoot(ds));
			backendServer.registerHandler("register_device", new CHandlerRegisterDevice(ds, encriptionCode, minClientVersionAllowed));
			backendServer.registerHandler("register_user_anonymous", new CHandlerRegisterUserAnonymous(ds, encriptionCode, minClientVersionAllowed));
			backendServer.registerHandler("register_session", new CHandlerRegisterSession(ds, encriptionCode, minClientVersionAllowed, mSessionManager));
			backendServer.registerHandler("register_game_data", new CHandlerRegisterGameData(ds, minClientVersionAllowed, mSessionManager));
			backendServer.registerHandler("update_session", new CHandlerUpdateSession(ds, minClientVersionAllowed, mSessionManager));
        }
		catch (Exception e)
		{
			System.err.println("Register handler Null pointer exception: " + e.getMessage());
            return;
		}
		
		backendServer.startOnPort(port);
	}

}
