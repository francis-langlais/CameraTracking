import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class UDPNetworkInterface {

	static final int MESSAGE_START = 0;
	static final int MESSAGE_STOP = 1;

	private InetAddress remoteAddress;
	private DatagramSocket socket;
	private int port;
	private int bufferSize;

	public UDPNetworkInterface(byte[] IPaddress, int remotePort, int localPort, int buffer) {

		try {
			remoteAddress = InetAddress.getByAddress(IPaddress);
			socket = new DatagramSocket(localPort);
		} catch (SocketException e) {
			System.out.println("error initializing the server: " + e);
			System.exit(1);
		} catch (UnknownHostException e) {
			System.out.println("error converting the IP address: " + e);
			System.exit(1);
		}

		port = remotePort;
		bufferSize = buffer;

	}

//	public UDPNetworkInterface(int localPort, int buffer) {
//
//		try {
//			socket = new DatagramSocket(localPort);
//		} catch (SocketException e) {
//			System.out.println("error initializing the server: " + e);
//			System.exit(1);
//		}
//
//		bufferSize = buffer;
//	}

	public void SendPacket(byte[] data) {

		DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, port);

		try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("error sending the packet: " + e);
			System.exit(1);
		}

	}

	public byte[] ReceivePacket() {

		byte[] temp = new byte[bufferSize];
		ByteBuffer buffer;
		DatagramPacket packet = new DatagramPacket(temp, temp.length);

		try {
			socket.receive(packet);
		} catch (IOException e) {
			System.out.println("Coud not receive a packet");
		}

		buffer = ByteBuffer.allocate(packet.getLength());
		buffer.put(Arrays.copyOf(packet.getData(), packet.getLength()));

		return buffer.array();
	}

	public void close() {
		socket.close();
	}

	public void sendMessage(int message) {
		SendPacket(ByteBuffer.allocate(4).putInt(message).array());
	}

	public int receiveMessage() {
		byte[] temp = new byte[bufferSize];
		ByteBuffer buffer;
		DatagramPacket packet = new DatagramPacket(temp, temp.length);

		try {
			socket.receive(packet);
		} catch (IOException e) {
			System.out.println("Coud not receive a packet");
		}

		buffer = ByteBuffer.allocate(packet.getLength());
		buffer.put(Arrays.copyOf(packet.getData(), packet.getLength()));

		if (buffer.array().length == 4) {
			return ByteBuffer.wrap(buffer.array()).getInt();
		} else {
			return -1;
		}
	}
}
