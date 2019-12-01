import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

// Remote interface for the RegistrationService
public interface RegistrationService extends Remote
{
	int register(RemoteNode c) throws RemoteException;
	ArrayList<RemoteNode> getDetectionNodeList() throws RemoteException;
	boolean disconnect(RemoteNode c) throws RemoteException;
}
