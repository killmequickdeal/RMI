import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;


public class DetectionNode extends UnicastRemoteObject implements Serializable, RemoteNode
{
	private static final long serialVersionUID = -905645444505287895L;
	private static final int simulationTime = 10500;

	public int id;
	public int logical_clock_time = 0;
	public Queue<Message> inputQueue = new ConcurrentLinkedQueue<>();
	public Queue<Message> workQueue = new ConcurrentLinkedQueue<>();
	public boolean byzantine_behavior = true;
	public int messages_sent = 0;
	public int messages_received = 0;
	public int internal_events = 0;
	public int messages_checked = 0;
	public int anomalies_detected = 0;
	public int messages_generated = 0;
	public int clock_adjustments = 0;
	public int messages_deleted = 0;

	private Queue<Message> outputQueue = new ConcurrentLinkedQueue<>();
	private Random random_generator = new Random();
	private PrintWriter writer;
	private RegistrationService m;
	private ArrayList<RemoteNode> neighbors;

	public DetectionNode(int id) throws RemoteException, FileNotFoundException, UnsupportedEncodingException {
		this.id = id;
		writer = new PrintWriter(this.id + "_info_3min_" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt", "UTF-8");
	}

	public void detect_anomaly() {
		/*
		Remove an item from the work queue, if its value is zero it is an anomaly
		If the node is displaying byzantine behavior, an anomaly results in the work queue being dropped.
		 */
		logical_clock_time++;
		internal_events++;

		if(!workQueue.isEmpty()) {
			messages_checked++;
			Message msg = workQueue.remove();
			if (msg.body == 0) {
				if(byzantine_behavior){
					messages_deleted += workQueue.size();
					workQueue.clear();
				}
				anomalies_detected++;
			}
		}
	}

	public void generate_message() {
		/*
		add a message to the output queue
		 */
		logical_clock_time++;
		internal_events++;
		messages_generated++;

		outputQueue.add(new Message(random_generator.nextInt(1000)));
	}

	public void send_message() throws RemoteException {
		/*
		Randomly select a neighbor to send a message to
		Remove a message from the output queue
		add the message to the neighbors input queue
		 */
		logical_clock_time++;
		if (!outputQueue.isEmpty())
		{
			messages_sent++;
			Message msg = outputQueue.remove();
			msg.logical_clock_time = logical_clock_time;
			msg.sender_id = id;
			try
			{
				neighbors.get(random_generator.nextInt(neighbors.size())).addMessage(msg);
			} catch (UnmarshalException ex) {
				System.out.println("Neighbor is unavailable to receive message, this may happen during DN failure or near end of simulation");
			}
		}
	}

	public void addMessage(Message m) {
		inputQueue.add(m);
	}

	public void receive_message() {
		/*
		grab a message from the input queue, add it to the work queue
		If the logical clock time on the message is greater than the current nodes' logical clock
		update the logical clock to msg_clock + 1
		 */
		if(!inputQueue.isEmpty()){
			messages_received++;
			Message msg = inputQueue.remove();
			workQueue.add(msg);
			if(msg.logical_clock_time > logical_clock_time){
				clock_adjustments++;
				logical_clock_time = msg.logical_clock_time+1;
			} else {
				logical_clock_time++;
			}
		}
	}

	public void waitForAllClients() throws RemoteException {
		try
		{
			// connect with the Registration server
			m = (RegistrationService) Naming.lookup("//localhost:1997/Server");

			// register
			int result = m.register(this);
			System.out.println("# nodes currently connected: " + result);
			System.out.println("Waiting for all four Detection nodes to register");

			while (m.getDetectionNodeList().size() < 4)
			{
				Thread.sleep(100);
			}

			// get the stubs for all nodes
			neighbors = m.getDetectionNodeList();
			// remove yourself since we dont want to paas messages to ourselves
			neighbors.remove(this);
			assert (neighbors.size() == 3);
			// run the simulation
			run();
		} catch (NotBoundException | MalformedURLException | InterruptedException | RemoteException ex) {
			ex.printStackTrace();
		} finally {
			m.disconnect(this);
			writer.close();
		}
	}

	public void run() throws RemoteException
	{
		System.out.println("STARTING EXECUTION");

		// make a timer task on a separate thread which will log execution info every 1 second
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				writer.println(id + "," + logical_clock_time + "," + messages_received + "," + messages_sent + "," + messages_generated + "," + messages_checked + "," + messages_deleted + "," + internal_events + "," + anomalies_detected + "," + clock_adjustments);
			}
		}, 0, 1000);

		long startTime = System.currentTimeMillis();

		// execute until we hit the amount of elapsed time specified for the simulation
		while(System.currentTimeMillis()-startTime<simulationTime)
		{
			// make a random number and execute the case it corresponds to
			int rand = random_generator.nextInt(3);
			switch (rand)
			{
				case 0:
					send_message();
					break;
				case 1:
					receive_message();
					break;
				case 2:
					int internal_event_choice = random_generator.nextInt(2);
					switch (internal_event_choice)
					{
						case 0:
							generate_message();
							break;
						case 1:
							detect_anomaly();
							break;
					}
			}
		}
	}

	public static void main(String[] args) throws InterruptedException, RemoteException, UnsupportedEncodingException, FileNotFoundException
	{
		int id = Integer.parseInt(args[0]);
		System.setSecurityManager(new SecurityManager());

		DetectionNode c = new DetectionNode(id);
		c.waitForAllClients();
		System.exit(0);
	}
}
