import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


public class Test extends UnicastRemoteObject implements Runnable, RemoteTester, Serializable
{
	int id;
	ArrayList<RemoteTester> testers = new ArrayList<>();
	public Queue<Message> inputQueue = new LinkedBlockingQueue<>();

	public Test(int i) throws RemoteException {
		this.id = i;
	}

	public void addMessage(Message m) {
		inputQueue.add(m);
	}

	public void run()
	{
		try
		{
			for (int i = 0; i < 4; i++)
			{
				if (i != id)
				{
					RemoteTester t = (RemoteTester) Naming.lookup("//localhost:1997/Tester" + i);
					testers.add(t);
				}
			}

			Message m = new Message(id);
			for(RemoteTester t: testers) {
				t.addMessage(m);
			}
			Thread.sleep(1000);


			System.out.println("MY ID: " + id);
			System.out.println("QUEUESIZE: "  + inputQueue.size());
			for (Message msg: inputQueue) {
				System.out.println(msg.body);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException
	{
		System.setSecurityManager(new SecurityManager());
		int port = 1997;
		ArrayList<Thread> threadList = new ArrayList<>();

		try {
			System.out.println("Creating a com.Market Server");
			String name = "//localhost:"+port+"/Server";
			LocateRegistry.createRegistry(port);


			System.out.println("BankServer: binding it to name " + name);

			System.out.println("com.Market Server Ready!");

			Thread.sleep(2000);
			for (int i = 0; i < 4; i++)
			{
				Test t = new Test(i);
				Naming.rebind("//localhost:1997/Tester"+i, t);
				threadList.add(new Thread(t));
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		for (Thread t: threadList)
		{
			t.start();
		}

		Thread.sleep(10000);

		for (Thread t: threadList) {
			t.interrupt();
		}
		System.exit(0);
	}

}