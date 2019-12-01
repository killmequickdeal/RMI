import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTester extends Remote
{
	public void addMessage(Message m) throws RemoteException;
}