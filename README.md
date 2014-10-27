DownloadManager
================

A download manager library written in Java<br>

This is not a multi-part download manager. That means, it does not have the capabilty to download a single file using multiple connection. But it can download multiple files simultaneously using multiple connections.<br>

If you are familiar with download managers, you should find the source code is self-explaining.<br>
I have included a very short test as an example. It will download this repo's master.zip using only three lines of code. For clarity sake the test doesn't print any message when the download has finished. The easiest way to do so is to derive the ```DownloadManager``` class and then override the ```notifyFinish``` method.<br>

Have fun!

