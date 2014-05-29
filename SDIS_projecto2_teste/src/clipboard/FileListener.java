package clipboard;

import gui.FileTransferProgress;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class FileListener extends Thread {
	private boolean go = true;
	public InetAddress hostAddress;
	private int hostPort;
	public byte availableContentType;
	private Socket socket = null;

	public FileListener(int port) {
		this.hostPort = port;
	}

	@Override
	public void run() {
		try {
			while (go) {
				synchronized (this) {
					while (socket == null || socket.isClosed())
						wait();
				}
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				BufferedInputStream bis = new BufferedInputStream(
						socket.getInputStream());
				DataInputStream dis = new DataInputStream(bis);
				switch (availableContentType) {
				case ClipboardListener.TEXT:
					String str = dis.readUTF();
					clipboard.setContents(new DataHandler(str,
							DataFlavor.stringFlavor.getMimeType()), null);
					break;
				case ClipboardListener.IMAGE:
					BufferedImage img = ImageIO.read(bis);
					clipboard.setContents(new ImageTransferable(img), null);
					break;
				case ClipboardListener.FILES:
					receiveFiles(dis, bis);
					break;
				}
				dis.close();
				socket.close();
				// TODO: passar para clipboard
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}

	
	private void receiveFiles(DataInputStream dis, BufferedInputStream bis)
			throws IOException {
		FileTransferProgress progressMonitor = new FileTransferProgress();
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showSaveDialog(progressMonitor);
		File saveFolder;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            saveFolder = fc.getSelectedFile();
        } else {
        	System.out.println("Operation cancelled");
            return;
        }
		try {
			while (true) {
				String filePath = dis.readUTF();
				filePath = saveFolder.getAbsolutePath() + '/' + filePath; //prepend selected folder path
				File file = new File(filePath);

				file.getAbsoluteFile().getParentFile().mkdirs();
				long fileLength = dis.readLong();
				int fileLengthMB = (int) (fileLength / 1024 / 1024);
				progressMonitor.progressBar.setMaximum(fileLengthMB);
				progressMonitor.progressBar.setValue(0);
				boolean flagCanceled = false;

				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				for (long i = 0; i < fileLength; i++) {
					if (progressMonitor.isCanceled()) {
						flagCanceled = true;
						break;
					}
					bos.write(bis.read());
					if ((i / 1024) % 1024 == 0) { // one MB
													// elapsed,
													// update
													// progress
													// monitor
						int progressMB = ((int) i / 1024 / 1024);
						progressMonitor.progressBar.setValue(progressMB);
						progressMonitor.progressBar.setString(progressMB
								+ "MB / " + fileLengthMB + "MB");
					}
				}
				bos.close();
				if (flagCanceled) {
					file.delete();
					break;
				} else
					progressMonitor.output.insert(
							"Transfered " + file.getName() + "\n", 0);
			}
		} catch (EOFException e) { // end of stream, do nothing
		}
		progressMonitor.close();
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
		super.interrupt();
	}

	public synchronized void requestFile() {
		try {
			if (socket == null || socket.isClosed()) {
				socket = new Socket(hostAddress, hostPort);
				notify();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
