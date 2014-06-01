package clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

public class ClipboardHandler extends Thread {
	private boolean go = true;
	private ServerSocket socket;

	private static final Executor exec = Executors.newCachedThreadPool();

	public ClipboardHandler(int port) {
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (go) {
			try {
				final Socket connection = socket.accept();
				Runnable task = new Runnable() {
					@Override
					public void run() {
						sendClipboard(connection);
					}
				};
				exec.execute(task);
			} catch (IOException e) {
				if (!(e instanceof SocketException)) // expected (close socket
														// on close thread)
					e.printStackTrace();
			}
		}
	}

	public void sendClipboard(Socket socket) {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			Transferable contents = clipboard.getContents(null);
			if (contents == null)
				return;
			BufferedOutputStream bos = new BufferedOutputStream(
					socket.getOutputStream());
			DataOutputStream dos = new DataOutputStream(bos);
			switch (ClipboardFlavorChangeListener.getContentType(contents)) {
			case ClipboardFlavorChangeListener.TEXT:
				String contentString = (String) contents
						.getTransferData(DataFlavor.stringFlavor);
				dos.writeUTF(contentString);
				break;
			case ClipboardFlavorChangeListener.IMAGE:
				BufferedImage img = (BufferedImage) contents
						.getTransferData(DataFlavor.imageFlavor);
				ImageIO.write(img, "png", bos);
				break;
			case ClipboardFlavorChangeListener.FILES:
				List<File> files = (List<File>) contents
						.getTransferData(DataFlavor.javaFileListFlavor);
				sendFiles(files, files.get(0).getParentFile(), bos, dos);
				break;
			default:
				System.out.println("Unsupported content type");
				break;
			}
			dos.close();
			socket.close();
		} catch (IOException | UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendFiles(List<File> files, File baseFolder,
			BufferedOutputStream bos, DataOutputStream dos) throws IOException {
		try {
			for (File file : files) {
				if (file.isDirectory()) {
					sendFiles(Arrays.asList(file.listFiles()), baseFolder, bos,
							dos);
				} else {
					String relativePath = getRelativePath(file, baseFolder);
					System.out.println("Sending file: " + relativePath);
					dos.writeUTF(relativePath);
					dos.writeLong(file.length());
					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);

					int readByte = 0;
					while ((readByte = bis.read()) != -1)
						bos.write(readByte); // write to socket stream
					bis.close();
				}
			}
		} catch (SocketException e) {
			System.out.println("File transfer interrupted");
		}
	}

	private static String getRelativePath(File file, File folder) {
		String filePath = file.getAbsolutePath().replace('\\', '/');
		String folderPath = folder.getAbsolutePath().replace('\\', '/');
		if (filePath.startsWith(folderPath)) {
			return filePath.substring(folderPath.length()
					+ (folderPath.contains("/") ? 1 : 0));
		} else {
			return null;
		}
	}

	@Override
	public synchronized void interrupt() {
		try {
			this.socket.close();
			this.go = false;
			super.interrupt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
