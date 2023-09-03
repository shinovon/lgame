package ru.lgame.launcher.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import ru.lgame.launcher.utils.WebUtils;
import ru.lgame.launcher.utils.logging.Log;

public class MultiThreadedDownloader {
	private ArrayList<Stack<DownloadTask>> tasksList = new ArrayList<Stack<DownloadTask>>();
	private ArrayList<DownloadThread> threadsList = new ArrayList<DownloadThread>();
	private int addIndex;
	private Object addLock = new Object();
	private Object waitLock = new Object();
	private Updater updater;
	private String text;
	private int downloaded;
	private int total;
	private Exception exception;
	private boolean listen;
	
	public MultiThreadedDownloader(int threads, int total, Updater updater, String text, boolean listen) {
		init(threads);
		this.total = total;
		this.updater = updater;
		this.text = text;
		this.listen = listen;
	}

	private class DownloadTask {
		String url;
		String dir;
		public DownloadTask(String url, String dir) {
			this.url = url;
			this.dir = dir;
		}
	}
	
	private class DownloadThread extends Thread {
		private Stack<DownloadTask> tasks;
		private WebUtils httpClient;
		
		DownloadThread(int i) {
			super("DT-" + i);
			tasks = tasksList.get(i);
			httpClient = new WebUtils();
			if(listen) httpClient.setListener(updater);
		}
		
		public void run() {
			while(true) {
				a: {
					try {
						if(tasks.size() > 0) {
							DownloadTask task = tasks.firstElement();
							boolean success = false;
							int attempts = 0;
							while(!success) {
								try {
									httpClient.download(task.url, task.dir);
									success = true;
								} catch (IOException e) {
									if(attempts >= 2) {
										if(exception == null) {
											exception = e;
										}
										MultiThreadedDownloader.this.stop();
										break a;
									}
									Log.warn("download io err: " + e.toString() + ", retrying.. (" + attempts +  ")");
									attempts++;
									Thread.sleep(500);
								}
							}
							tasks.removeElementAt(0);
							updater.tasksDone++;
							downloaded++;
							if(text != null) updater.uiInfo(text + " ("+downloaded+"/"+total+")");
						} else {
							synchronized(addLock) {
								addLock.wait();
							}
						}
					} catch (InterruptedException e) {
						Log.debug(getName() + " interrupted");
						synchronized(waitLock) {
							waitLock.notify();
						}
						return;
					} catch (Exception e) {
						Log.error("Error while multi threaded downloading", e);
					}
				}
				synchronized(waitLock) {
					waitLock.notify();
				}
			}
		}
		
	}
	
	private void init(int threads) {
		for(int i = 0; i < threads; i++) {
			tasksList.add(new Stack<DownloadTask>());
			DownloadThread dt = new DownloadThread(i);
			threadsList.add(dt);
			dt.start();
		}
	}
	
	public void stop() {
		Log.debug("MTD stop");
		for(Stack<DownloadTask> tasks: tasksList) {
			tasks.clear();
		}
		for(DownloadThread dt: threadsList) {
			dt.interrupt();
		}
		synchronized(addLock) {
			addLock.notifyAll();
		}
	}

	public void add(String url, String dir) {
		if(addIndex == tasksList.size()) addIndex = 0;
		tasksList.get(addIndex++).add(new DownloadTask(url, dir));
		synchronized(addLock) {
			addLock.notifyAll();
		}
	}
	
	public void lock() throws Exception {
		try {
			synchronized(addLock) {
				addLock.notifyAll();
			}
			while(true) {
				a: {
					for(Stack<DownloadTask> tasks: tasksList) {
						if(!tasks.isEmpty()) {
							break a;
						}
					}
					if(exception != null) {
						throw exception;
					}
					return;
				}
				synchronized(waitLock) {
					waitLock.wait();
				}
			}
		} catch (InterruptedException e) {
		}
	}
}
