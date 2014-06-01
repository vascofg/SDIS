package clipboard;

import interfaces.SendClipboardMessage;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;

import message.Message;

public class ClipboardFlavorChangeListener implements FlavorListener {

	static final byte IMAGE = 0;
	static final byte FILES = 1;
	static final byte TEXT = 2;

	public static final String[] typeText = { "Image", "Files", "Text" };

	// save add message to queue interface
	private SendClipboardMessage messageSender;

	public ClipboardFlavorChangeListener(SendClipboardMessage messageSender) {
		this.messageSender = messageSender;
	}

	@Override
	public void flavorsChanged(FlavorEvent arg0) {
		try {
			Clipboard clipboard = (Clipboard) arg0.getSource();
			Transferable contents = clipboard.getContents(null);
			Byte type = getContentType(contents);
			if (type != null)
				messageSender.sendClipboardMessage(Message.haveClipboard(type));
		} catch (IllegalStateException e) {
			System.out.println("Couldn't get clipboard contents");
		}

	}

	public static Byte getContentType(Transferable contents) {
		Byte type = null;
		if (contents.isDataFlavorSupported(DataFlavor.imageFlavor))
			type = IMAGE;
		else if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			type = FILES;
		else if (contents.isDataFlavorSupported(DataFlavor.stringFlavor))
			type = TEXT;
		return type;
	}
}
