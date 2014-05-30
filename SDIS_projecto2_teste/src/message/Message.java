package message;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Message {
	public static final byte MOUSE_MOVE = 1;
	public static final byte MOUSE_PRESS = 2;
	public static final byte MOUSE_RELEASE = 3;
	public static final byte MOUSE_SCROLL = 4;
	public static final byte KEY_PRESS = 5;
	public static final byte KEY_RELEASE = 6;
	public static final byte EDGE = 7;
	public static final byte CONNECT = 8;
	public static final byte DISCONNECT = 9;
	public static final byte RESOLUTION = 10;
	public static final byte ALIVE = 11;
	public static final byte CLIPBOARD_HAVE = 12;
	public static final byte CLIPBOARD_GET = 13;
	public static final byte CLIPBOARD_ANNOUNCE = 14;
	public static final byte LEAVE = 15;

	private static final byte AVERAGE_NO_BYTES = 6; // número previsto médio de
													// bytes por msg

	private List<Byte> bytes;
	private InetAddress remoteAddress;

	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}

	private void setRemoteAddress(InetAddress address) {
		this.remoteAddress = address;
	}

	private static short getMessageLength(byte msgType) { // obtém tamanho da
															// mensagem por tipo
		switch (msgType) {
		case CLIPBOARD_ANNOUNCE:
			return 6; //type + content type + ip address (4 bytes)
		case MOUSE_MOVE:
		case RESOLUTION:
			return 5;
		case MOUSE_PRESS:
		case MOUSE_RELEASE:
		case KEY_PRESS:
		case KEY_RELEASE:
		case EDGE:
			return 3;
		case MOUSE_SCROLL:
		case CLIPBOARD_HAVE:
			return 2;
		case CONNECT:
		case DISCONNECT:
		case ALIVE:
		case LEAVE:
			return 1;
		default:
			return 0;
		}
	}

	public Message(InputEvent event) { // constructor para mensagens de eventos
		this.bytes = new LinkedList<Byte>();
		switch (event.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			bytes.add(Message.MOUSE_PRESS);
			bytes.addAll(intToByteList(InputEvent
					.getMaskForButton(((MouseEvent) event).getButton()), 2));
			break;
		case MouseEvent.MOUSE_RELEASED:
			bytes.add(Message.MOUSE_RELEASE);
			bytes.addAll(intToByteList(InputEvent
					.getMaskForButton(((MouseEvent) event).getButton()), 2));
			break;
		case MouseWheelEvent.MOUSE_WHEEL:
			bytes.add(Message.MOUSE_SCROLL);
			bytes.addAll(intToByteList(
					((MouseWheelEvent) event).getWheelRotation(), 1));
			break;
		case KeyEvent.KEY_PRESSED:
			bytes.add(Message.KEY_PRESS);
			bytes.addAll(intToByteList(((KeyEvent) event).getKeyCode(), 2));
			break;
		case KeyEvent.KEY_RELEASED:
			bytes.add(Message.KEY_RELEASE);
			bytes.addAll(intToByteList(((KeyEvent) event).getKeyCode(), 2));
			break;
		}
	}

	public Message() { // constructor default (para mensagem indicativa do
						// update do rato)
	}

	public Message(byte messageType) { // constructor para mensagens
										// de controlo
		this.bytes = new LinkedList<Byte>();
		bytes.add(messageType);
	}

	public static Message resolution() {
		Message msg = new Message(Message.RESOLUTION);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		msg.bytes.addAll(intToByteList(dim.width, 2));
		msg.bytes.addAll(intToByteList(dim.height, 2));
		return msg;
	}

	public static Message edge(byte edge, int percentage) {
		Message msg = new Message(Message.EDGE);
		msg.bytes.add(edge);
		msg.bytes.addAll(intToByteList(percentage, 1));
		return msg;
	}

	public static Message haveClipboard(byte contentType) {
		Message msg = new Message(Message.CLIPBOARD_HAVE);
		msg.bytes.add(contentType);
		return msg;
	}

	public static Message announceClipboard(byte contentType, InetAddress addr) {
		Message msg = new Message(Message.CLIPBOARD_ANNOUNCE);
		msg.bytes.add(contentType);
		for(byte addrByte : addr.getAddress())
			msg.bytes.add(addrByte);
		return msg;
	}

	public Message(List<Byte> bytes) {
		this.bytes = new LinkedList<Byte>();
		this.bytes.addAll(bytes);
	}

	public static Message mouseDelta(Point p) // constructor para mensagem de
												// mouse deltas
	{
		Message msg = new Message(Message.MOUSE_MOVE);
		msg.bytes.addAll(intToByteList(p.x, 2)); // x
		msg.bytes.addAll(intToByteList(p.y, 2)); // y
		return msg;
	}

	public boolean isControl() {
		switch (getType()) {
		case EDGE:
		case CONNECT:
		case RESOLUTION:
		case DISCONNECT:
		case ALIVE:
		case CLIPBOARD_HAVE:
		case CLIPBOARD_GET:
		case LEAVE:
			return true;
		default:
			return false;
		}
	}

	// Só guarda valores negativos até - 2^(size*7)
	public static List<Byte> intToByteList(int num, int size) {
		List<Byte> bytes = new ArrayList<>(size);
		for (int i = size - 1; i >= 0; i--)
			bytes.add((byte) ((num >> Byte.SIZE * i) & 0xFF));
		return bytes;
	}

	public static int byteArrayToUnsignedInt(byte[] b) {
		int value = 0;
		for (int i = b.length - 1, j = 0; i >= 0; i--, j++)
			value ^= (b[j] & 0xFF) << Byte.SIZE * i;
		return value;
	}

	public static int byteArrayToSignedInt(byte[] b) {
		int counter = 3;
		byte[] temp = new byte[4]; // byte array do tamanho de um int
		for (int i = b.length - 1; i >= 0; i--, counter--) { // passa bytes de b
																// para temp
			temp[counter] = b[i];
		}
		if (b[0] < 0) { // se número negativo, extende sinal
			for (; counter >= 0; counter--) {
				temp[counter] = (byte) 0xFF;
			}
		}
		int value = ByteBuffer.wrap(temp).getInt(); // converte
		return value;
	}

	public byte getType() {
		return bytes.get(0); // primeiro elemento é o type
	}

	public Point getMouseDelta() {
		byte[] bytes = new byte[2];
		Point p = new Point();
		Iterator<Byte> t = this.bytes.listIterator(1);
		bytes[0] = (byte) t.next();
		bytes[1] = (byte) t.next();
		p.x = byteArrayToSignedInt(bytes);
		bytes[0] = (byte) t.next();
		bytes[1] = (byte) t.next();
		p.y = byteArrayToSignedInt(bytes);
		return p;
	}

	public int getMouseButtons() {
		byte[] bytes = new byte[2];
		Iterator<Byte> t = this.bytes.listIterator(1);
		bytes[0] = (byte) t.next();
		bytes[1] = (byte) t.next();
		int mouseButtons = byteArrayToUnsignedInt(bytes);
		return mouseButtons;
	}

	public int getMouseScroll() {
		byte[] bytes = new byte[1];
		Iterator<Byte> t = this.bytes.listIterator(1);
		bytes[0] = (byte) t.next();
		int mouseScroll = byteArrayToSignedInt(bytes);
		return mouseScroll;
	}

	public int getKeyCode() {
		byte[] bytes = new byte[2];
		Iterator<Byte> t = this.bytes.listIterator(1);
		bytes[0] = (byte) t.next();
		bytes[1] = (byte) t.next();
		int keyCode = byteArrayToUnsignedInt(bytes);
		return keyCode;
	}
	
	public int getPercentage() {
		byte[] bytes = new byte[1];
		Iterator<Byte> t = this.bytes.listIterator(2);
		bytes[0] = (byte) t.next();
		int percentage = byteArrayToUnsignedInt(bytes);
		return percentage;
	}

	public Dimension getResolution() {
		byte[] bytes = new byte[2];
		Dimension dim = new Dimension();
		Iterator<Byte> t = this.bytes.listIterator(1);
		bytes[0] = (byte) t.next();
		bytes[1] = (byte) t.next();
		dim.width = byteArrayToSignedInt(bytes);
		bytes[0] = (byte) t.next();
		bytes[1] = (byte) t.next();
		dim.height = byteArrayToSignedInt(bytes);
		return dim;
	}
	
	public InetAddress getAddress() throws UnknownHostException {
		byte[] bytes = new byte[4];
		Iterator<Byte> t = this.bytes.listIterator(2);
		bytes[0] = (byte) t.next();
		bytes[1] = (byte) t.next();
		bytes[2] = (byte) t.next();
		bytes[3] = (byte) t.next();
		return InetAddress.getByAddress(bytes);
	}

	public byte getEdge() {
		byte[] bytes = new byte[1];
		Iterator<Byte> t = this.bytes.listIterator(1);
		bytes[0] = (byte) t.next();
		byte edge = (byte) byteArrayToSignedInt(bytes);
		return edge;
	}
	
	public byte getContentType() {
		byte[] bytes = new byte[1];
		Iterator<Byte> t = this.bytes.listIterator(1);
		bytes[0] = (byte) t.next();
		byte contentType = (byte) byteArrayToSignedInt(bytes);
		return contentType;
	}

	public static byte[] getPacket(List<Message> messages) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(messages.size()
				* AVERAGE_NO_BYTES);
		for (final Message msg : messages) {
			if (msg.bytes != null) { // mensagem válida
				for (final byte msgByte : msg.bytes)
					bos.write(msgByte);
			}
		}
		return bos.toByteArray();
	}

	public byte[] getBytes() {
		Iterator<Byte> t = this.bytes.listIterator();
		byte[] bytes = new byte[this.bytes.size()];
		for (int i = 0; i < this.bytes.size(); i++)
			bytes[i] = t.next();
		return bytes;
	}

	public static void decodePacket(byte[] bytes, int len,
			List<Message> eventMessages, List<Message> controlMessages,
			InetAddress address) {
		Message currentMessage;
		List<Byte> messageBytes = new LinkedList<>();
		int i = 0, msgLen;
		while (i < len) {
			msgLen = Message.getMessageLength(bytes[i]);
			for (int j = 0; j < msgLen; j++)
				messageBytes.add(bytes[i++]);

			currentMessage = new Message(messageBytes);
			if (currentMessage.isControl()) {
				// guarda peer de onde veio
				currentMessage.setRemoteAddress(address);
				controlMessages.add(currentMessage);
			} else
				eventMessages.add(currentMessage);
			messageBytes.clear();
		}
	}
}
