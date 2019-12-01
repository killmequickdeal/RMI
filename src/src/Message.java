import java.io.Serializable;

class Message implements Serializable
{
	public int body;
	public int logical_clock_time;
	public int sender_id;

	Message (int body) {
		this.body = body;
	}
}