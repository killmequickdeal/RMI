import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Server extends UnicastRemoteObject implements RegistrationService
{

	private String name;
	ArrayList<RemoteNode> detectionNodeList = new ArrayList<>();

	public Server(String name) throws RemoteException
	{
		this.name = name;
	}

	// add a node to the list of currently connected nodes
	public int register(RemoteNode c)
	{
		detectionNodeList.add(c);
		return detectionNodeList.size();
	}

	// getter for currently connected nodes
	public ArrayList<RemoteNode> getDetectionNodeList() {
		return detectionNodeList;
	}

	// remove a node from the list of currently connected nodes
	public boolean disconnect(RemoteNode c) {
		return detectionNodeList.remove(c);
	}

	public static void main (String args[]) {
		System.setSecurityManager(new SecurityManager());
		int port = 1997;
		try {
			// start Registration Server and bind it to the registry
			String name = "//localhost:"+port+"/Server";
			Server server = new Server(name);
			LocateRegistry.createRegistry(port);
			Naming.rebind(name, server);

			System.out.println("Server Ready!");
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
