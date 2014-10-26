class TestDownloadManager {
	public static void main(String[] args) {
		DownloadManager dm = new DownloadManager();
		dm.add("https://github.com/chrishadi/DownloadManager/archive/master.zip");
		dm.startAll();
	}
}
