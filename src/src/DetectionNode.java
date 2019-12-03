import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;


public class DetectionNode extends UnicastRemoteObject implements Serializable, RemoteNode
{
	private static final long serialVersionUID = -905645444505287895L;
	private static final int simulationTime = 300500;
	private boolean byzantine_behavior = true;
	private int probability1 = 10;
	private int probability2 = 20;
	private int probability3 = 50;

	private int id;
	private Queue<Message> inputQueue = new ConcurrentLinkedQueue<>();
	private Queue<Message> workQueue = new ConcurrentLinkedQueue<>();

	private int logical_clock_time = 0;
	private int messages_sent = 0;
	private int messages_received = 0;
	private int internal_events = 0;
	private int messages_checked = 0;
	private int anomalies_detected = 0;
	private int messages_generated = 0;
	private int clock_adjustments = 0;
	private int messages_deleted = 0;



	private Queue<Message> outputQueue = new ConcurrentLinkedQueue<>();
	private Random random_generator = new Random();
	private PrintWriter writer;
	private RegistrationService registrationService;
	private ArrayList<RemoteNode> neighbors;

	public DetectionNode(int id) throws RemoteException, FileNotFoundException, UnsupportedEncodingException {
		this.id = id;
		writer = new PrintWriter(this.id + "_info_5min_" + probability1+ "_" + probability2 + "_" + probability3 + ".txt", "UTF-8");
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
			} catch (UnmarshalException | ConnectException ex) {
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

	private void updateNeighbors() throws RemoteException {
		neighbors = registrationService.getDetectionNodeList();
		// remove yourself since we don't want to pass messages to ourselves
		neighbors.remove(this);
	}

	public void waitForAllClients() throws RemoteException {
		try
		{
			// connect with the Registration server
			registrationService = (RegistrationService) Naming.lookup("//localhost:1997/Server");

			// register
			int result = registrationService.register(this);
			System.out.println("# nodes currently connected: " + result);
			System.out.println("Waiting for all four Detection nodes to register");

			while (registrationService.getDetectionNodeList().size() < 4)
			{
				Thread.sleep(100);
			}

			// get the stubs for other nodes
			updateNeighbors();

			// run the simulation
			run();
		} catch (NotBoundException | MalformedURLException | InterruptedException | RemoteException ex) {
			ex.printStackTrace();
		} finally {
			registrationService.disconnect(this);
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
			int rand = random_generator.nextInt(100);
			if (rand >= 0 && rand < probability1) {
				send_message();
			} else if (rand >= probability1 && rand < probability2) {
				receive_message();
			} else {
				int internal_event_choice = random_generator.nextInt(100);
				if (internal_event_choice >= 0 && internal_event_choice < probability3) {
					generate_message();
				} else if (internal_event_choice >= probability3 && internal_event_choice < 100) {
					detect_anomaly();
				}
			}
		}
	}

	public static void main(String[] args) throws RemoteException, UnsupportedEncodingException, FileNotFoundException
	{
		int id = Integer.parseInt(args[0]);
		System.setSecurityManager(new SecurityManager());

		DetectionNode c = new DetectionNode(id);
		c.waitForAllClients();
		System.exit(0);
	}
}
