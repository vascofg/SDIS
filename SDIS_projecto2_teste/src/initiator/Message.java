package initiator;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
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

	private static final byte AVERAGE_NO_BYTES = 2; // número previsto médio de
													// bytes por msg

	private LinkedList<Byte> bytes = new LinkedList<Byte>();

	public Message(InputEvent event) { // constructor para mensagens de eventos
		switch (event.getID()) {
		//TODO: completar
		case MouseEvent.MOUSE_MOVED:
		case MouseEvent.MOUSE_DRAGGED:
			bytes.add(Message.MOUSE_MOVE);
			break;
		case MouseEvent.MOUSE_PRESSED:
			bytes.add(Message.MOUSE_PRESS);
			break;
		case MouseEvent.MOUSE_RELEASED:
			bytes.add(Message.MOUSE_RELEASE);
			break;
		case MouseEvent.MOUSE_WHEEL:
			bytes.add(Message.MOUSE_SCROLL);
			break;
		case KeyEvent.KEY_PRESSED:
			bytes.add(Message.KEY_PRESS);
			break;
		case KeyEvent.KEY_RELEASED:
			bytes.add(Message.KEY_RELEASE);
			break;
		}
	}

	public Message(byte messageType) {
		// TODO: completar
	}

	//Só guarda valores negativos até - 2^(size*7)
	public static byte[] intToByteArray(int num, int size) {
		byte[] value = new byte[size];
		for (int i = size - 1, j = 0; i >= 0; i--, j++)
			value[j] = (byte) ((num >> Byte.SIZE * i) & 0xFF);
		return value;
	}

	public static int byteArrayToUnsignedInt(byte[] b) {
		int value = 0;
		for (int i = b.length - 1, j = 0; i >= 0; i--, j++)
			value ^= (b[j] & 0xFF) << Byte.SIZE * i;
		return value;
	}
	
	public static int byteArrayToSignedInt(byte[] b) {
		int value = 0;
		for (int i = b.length - 1, j = 0; i >= 0; i--, j++)
			value ^= (b[j]) << Byte.SIZE * i;
		return value;
	}

	static byte[] getPacket(List<Message> messages) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(messages.size()
				* AVERAGE_NO_BYTES);
		for (final Message msg : messages)
			for (final byte msgByte : msg.bytes)
				bos.write(msgByte);
		return bos.toByteArray();
	}
}
