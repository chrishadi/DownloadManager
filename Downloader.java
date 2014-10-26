import java.io.*;
import java.net.*;
import java.util.Date;

public class Downloader extends Thread {

	protected String url;
	protected String saveTo;
	protected DownloadManager manager = null;
	protected boolean pause = false;

	public long jobId;
	public Date startTime;
	public Date finishTime;
	public long downloaded;
	public boolean success;
	public Exception exception = null;

	public Downloader() {
	}

	public void run() {
		InputStream in = null;
		FileOutputStream out = null;
		try {
			this.startTime = new Date();
			URL url = null;
			url = new URL(this.url);

			byte[] buffer = new byte[256*1024];
			in = url.openStream();
			out = new FileOutputStream(this.saveTo);

			this.downloaded = 0;
			int read = 0;
			while (!this.isInterrupted() && 
				(read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				this.downloaded += read;

				if (this.pause) {
					synchronized(this) {
						while (this.pause) {
							this.wait();
						}
					}
				}
			}
		} catch (Exception e) {
			this.success = false;
			this.exception = e;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {}

			if (this.exception != null) {
				this.success = true;
				this.finishTime = new Date();
			}

			if (this.manager != null) {
				this.manager.notifyFinish(this);
			}
		}
	}

	public Downloader setManager(DownloadManager dm) {
		this.manager = dm;
		return this;
	}

	public Downloader assign(long jobId, String url, String saveTo) {
		this.jobId = jobId;
		this.url = url;
		this.saveTo = saveTo;
		return this;
	}

	public Downloader pause() {
		this.pause = true;
		return this;
	}

	public Downloader _resume() {
		this.pause = false;
		this.notify();
		return this;
	}

	public Downloader _stop() {
		this.interrupt();
		return this;
	}

}
