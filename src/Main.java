
import org.apache.commons.dbcp2.BasicDataSource;

import com.mozaicgames.core.CBackendRequestHandler;
import com.mozaicgames.core.CBackendServer;
import com.mozaicgames.executors.CRequestExecutorCheckDevice;
import com.mozaicgames.executors.CRequestExecutorCheckServer;
import com.mozaicgames.executors.CRequestExecutorRegisterDevice;
import com.mozaicgames.executors.CRequestExecutorRegisterGameResult;
import com.mozaicgames.executors.CRequestExecutorRegisterSession;
import com.mozaicgames.executors.CRequestExecutorRegisterUserAnonymous;
import com.mozaicgames.executors.CRequestExecutorUpdateDevice;
import com.mozaicgames.executors.CRequestExecutorUpdateSession;
import com.mozaicgames.executors.CRequestExecutorUpdateUserGameData;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendSessionCleanerScheduler;
import com.mozaicgames.utils.CBackendSessionManager;

public class Main 
{
	
	private static BasicDataSource									mDataSource = null;
	private static CBackendSessionManager 							mSessionManager = null;
	private static CBackendSessionCleanerScheduler					mSessionManagerCleaner = null;
	private static CBackendAdvancedEncryptionStandard 				mEncripter = null; 
	
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
        
        try 
        {
            final String encriptionCode = "mozadev123";
        	mDataSource = new BasicDataSource();
        	mEncripter = new CBackendAdvancedEncryptionStandard(encriptionCode, "AES");
        	mSessionManager = new CBackendSessionManager(mDataSource, mEncripter);
        	mSessionManagerCleaner = new CBackendSessionCleanerScheduler(mSessionManager);
        	mSessionManagerCleaner.startService();
        }
        catch (Exception e)
        {
        	System.err.println(e.getMessage());
            return;
        }
        
        mDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        mDataSource.setUrl("jdbc:mysql://localhost:3306/mozaic");
        mDataSource.setUsername("dev");
        mDataSource.setPassword("Mozaic123!");
        
        final String minClientVersionAllowed = "1.0.0";
        CBackendServer backendServer = new CBackendServer();
        
        CBackendRequestHandler requestHandler = null;
        
		try 
        {
			requestHandler = new CBackendRequestHandler(mDataSource, mSessionManager, minClientVersionAllowed, mEncripter);
			requestHandler.addRequestExecuter("register_device", new CRequestExecutorRegisterDevice());
			requestHandler.addRequestExecuter("register_user_anonymous", new CRequestExecutorRegisterUserAnonymous());
			requestHandler.addRequestExecuter("register_session", new CRequestExecutorRegisterSession());	
			requestHandler.addRequestExecuter("register_game_result", new CRequestExecutorRegisterGameResult());	
			requestHandler.addRequestExecuter("update_device", new CRequestExecutorUpdateDevice());
			requestHandler.addRequestExecuter("update_session", new CRequestExecutorUpdateSession());
			requestHandler.addRequestExecuter("update_usergame_data", new CRequestExecutorUpdateUserGameData());
			requestHandler.addRequestExecuter("check_device_status", new CRequestExecutorCheckDevice());
			requestHandler.addRequestExecuter("check_server_status", new CRequestExecutorCheckServer());
        }
		catch (Exception e)
		{
			System.err.println("Register handler Null pointer exception: " + e.getMessage());
            return;
		}
		
		backendServer.registerHandler("end_point", requestHandler);
		backendServer.startOnPort(port);
	}

}
