import java.rmi.Remote;
import java.rmi.RemoteException;

// Remote interface for DetectionNodes
public interface RemoteNode extends Remote
{
	void addMessage(Message m) throws RemoteException;
}
