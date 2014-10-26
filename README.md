DownloadManager
================

A download manager library written in Java<br>

This is not a multi-part download manager (and doesn't have the capability to download a file in multi-part). But it can download multiple files in multiple threads (one thread per file).<br>

If you are familiar with download managers, you should find the source code is self-explaining.<br>
I have included a very short test as an example. It will download this repo's master.zip only in three lines of code. If you are looking for a way to print message when the downloads has finished, the easiest way is to derive the DownloadManager class and override the notifyFinish method.<br>

Have fun!

