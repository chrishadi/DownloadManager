class TestDownloadManager {
	public static void main(String[] args) {
		DownloadManager dm = new DownloadManager();
		dm.add("https://github.com/chrishadi/download-manager/archive/master.zip");
		dm.startAll();
	}
}
