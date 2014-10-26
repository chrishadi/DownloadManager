import java.io.*;
import java.net.*;
import java.util.*;

public class DownloadManager {

	protected List<Downloader> workerList;
	protected List<String> urlList;
	protected List<Long> doneList;
	protected String dlDir = "";
	protected int maxWorker = 10;
	protected boolean paused = false;
	protected boolean stopped = false;
	protected boolean finish = false;
	protected int nextJobIndex = 0;

	public Date startTime = null;
	public Date finishTime = null;
	public int byteCount = 0;

	public DownloadManager() {
		this.workerList = new ArrayList<Downloader>();
		this.urlList = new ArrayList<String>();
		this.doneList = new ArrayList<Long>();
	}

	public DownloadManager(List<String> urls) {
		this();
		this.setUrlList(urls);
	}

	public DownloadManager(String[] urls) {
		this();
		this.setUrlList(urls);
	}

	public DownloadManager(List<String> urls, String dlDir) {
		this(urls);
		this.setDownloadDir(dlDir);
	}

	public DownloadManager(String[] urls, String dlDir) {
		this(urls);
		this.setDownloadDir(dlDir);
	}

	public DownloadManager setUrlList(List<String> urls) {
		this.urlList.clear();
		this.urlList.addAll(urls);
		return this;
	}

	public DownloadManager setUrlList(String[] urls) {
		return this.setUrlList(Arrays.asList(urls));
	}

	public DownloadManager setDoneList(List<Long> jobIds) {
		this.doneList.clear();
		this.doneList.addAll(jobIds);
		return this;
	}

	public DownloadManager setDoneList(Long[] jobIds) {
		return this.setDoneList(Arrays.asList(jobIds));
	}

	public DownloadManager setDownloadDir(String dir) {
		this.dlDir = dir.endsWith("/") ? dir : (dir + "/");
		return this;
	}

	public DownloadManager setMaxWorker(int number) {
		this.maxWorker = number;
		return this;
	}

	public boolean add(String url) {
		return this.urlList.add(url);
	}

	public String remove(int index) {
		Downloader worker = this.getWorker(index);
		if (worker != null) {
			Thread.State state = worker.getState();
			if (state != Thread.State.NEW && state != Thread.State.TERMINATED) {
				worker._stop();
			}
			this.workerList.remove(worker);
		}
		long id = this.createJobId(index, this.urlList);
		this.doneList.remove(id);
		return this.urlList.remove(index);
	}

	public Downloader getWorker(int index) {
		long id = this.createJobId(index, this.urlList);
		for (Downloader worker : this.workerList) {
			if (worker.jobId == id) {
				return worker;
			}
		}
		return null;
	}

	public DownloadManager startAll() {
		if (urlList.size() == 0) {
			System.out.println("DownloadManager not started.\n" +
				"No url was given.");
			return this;
		}

		this.initJob();

		return this;
	}

	protected void initJob() {
		// Reset
		this.paused = false;
		this.stopped = false;
		this.finish = false;
		this.nextJobIndex = 0;
		// Start the stopwatch
		this.startTime = new Date();

		int nRemainder = this.urlList.size() - this.doneList.size();
		int nWorker = Math.min(this.maxWorker, nRemainder);
		for (int i = 0; i < nWorker; i++) {
			while (this.nextJobIndex < this.urlList.size()) {
				long id = this.createJobId(this.nextJobIndex, this.urlList);
				if (!this.doneList.contains(id)) {
					this.spawnNewDownload(this.nextJobIndex);
					this.nextJobIndex++;
					break;
				}
				this.nextJobIndex++;
			}
		}
	}

	protected Downloader spawnNewDownload(int index) {
		long id = this.createJobId(index, this.urlList);
		String url = urlList.get(index);
		String saveTo = this.constructDestinationPath(this.dlDir, url);

		Downloader d = this.createDownloader();
		this.workerList.add(d);
		d.setManager(this);
		d.assign(id, url, saveTo);
		d.start();
		return d;
	}

	// Override this function to generate custom job ID
	protected long createJobId(int index, List<String> list) {
		return index;
	}

	// Override this in derivatif class to create custom implementation
	protected String constructDestinationPath(String dlDir, String url)
	{
		if (!dlDir.isEmpty() && !dlDir.endsWith("/")) {
			dlDir += "/";
		}
		return dlDir + this.extractFilenameFromUrl(url);
	}

	protected String extractFilenameFromUrl(String url) {
		int i = url.lastIndexOf('/');
		return (i != -1) ? url.substring(i+1) : url;
	}

	protected Downloader createDownloader() {
		return new Downloader();
	}

	public DownloadManager pauseAll() {
		if (!this.paused) {
			for (Downloader worker : this.workerList) {
				worker.pause();
			}
			this.paused = true;
		}
		return this;
	}

	public DownloadManager resumeAll() {
		if (this.paused)
		{
			for (Downloader worker : this.workerList) {
				worker._resume();
			}
			this.paused = false;
		}
		return this;
	}

	public DownloadManager stopAll() {
		if (!this.stopped)
		{
			this.stopped = true;
			for (Downloader worker : this.workerList) {
				worker._stop();
			}
		}
		return this;
	}

	public DownloadManager restartAll() {
		this.stopAll();
		this.startAll();
		return this;
	}

	public DownloadManager start(int index) {
		long id = this.createJobId(index, this.urlList);
		this.doneList.remove(id);
		if (this.workerList.size() < this.maxWorker) {
			this.paused = false;
			this.stopped = false;
			this.finish = false;
			Downloader worker = this.getWorker(index);
			if (worker != null) {
				if (worker.getState() == Thread.State.NEW) {
					worker.start();
				}
			} else {
				this.spawnNewDownload(index);
			}
		}
		return this;
	}

	public DownloadManager pause(int index) {
		Downloader worker = this.getWorker(index);
		if (worker != null) {
			worker.pause();
		}
		return this;
	}

	public DownloadManager resume(int index) {
		Downloader worker = this.getWorker(index);
		if (worker != null) {
			worker._resume();
		}
		return this;
	}

	public DownloadManager stop(int index) {
		Downloader worker = this.getWorker(index);
		if (worker != null) {
			worker._stop();
		}
		return this;
	}

	public DownloadManager restart(int index) {
		this.stop(index);
		this.start(index);
		return this;
	}

	public synchronized void notifyFinish(Downloader worker) {
		if (worker.success) {
			this.doneList.add(worker.jobId);
		}
		this.byteCount += worker.downloaded;
		this.workerList.remove(worker);
		if (!this.stopped) {
			while (this.nextJobIndex < this.urlList.size()) {
				long id = this.createJobId(this.nextJobIndex, this.urlList);
				if (this.doneList.contains(id)) {
					this.nextJobIndex++;
					continue;
				}
				this.spawnNewDownload(this.nextJobIndex);
				this.nextJobIndex++;
				return;
			}
		}
		if (this.workerList.isEmpty()) {
			// Stop the stopwatch
			this.finishTime = new Date();
			this.finish = true;
		}
	}

}
