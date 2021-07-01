package SmartParking.Server;

/*
	Author: Christian Stingone

	This class corresponds to the server that receives the requests, executes one thread per request through a threadpool
	and provides methods for inserting or removing elements from the data structure.
*/

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;

public class ThreadPooledServer implements Runnable {

	// Default port, you can change if necessary
	private int serverPort = 8080;							// default port	
	private ServerSocket serverSocket;						// socket where we receive the request
	private boolean isStopped = false;
	private ThreadPoolExecutor threadPool;					// threadpool to handle the request
	private volatile ConcurrentHashMap<String, String> park;	// park data structure
	private int N_available;									// number of parking spot

	/* ################################# Statistic ################################# */

	// for thread in waiting state
	private int waitingThreads = 0;							// number of threads in wait

	// for avg waiting time 
	private long waitingTime = 0;								// avg of waiting time
	private int waitingTotal = 0;								// number of thread used in avg


	public ThreadPooledServer(int available) {
		// default port = 8080
		this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();		// number of threads are not fixed
		this.N_available = available;
		this.park = new ConcurrentHashMap<String, String>(this.N_available); 
	} 

	public ThreadPooledServer(int port, int available) {
		this(available);
		this.serverPort = port;		// default = 8080
	}

	/* ################################# Getter ################################# */

	// check if the server is stopped
	public synchronized boolean isStopped() {
		return this.isStopped;
	}

	// get number of parks
	public int getN_available() {
		return N_available;
	}

	// get number of threads in waiting
	public int getWaitingThreads() {
		return waitingThreads;
	}

	// abg of waiting time
	public int getWaitingTime() {
		return (int)waitingTime;
	}

	/* ################################# Setter ################################# */

	public synchronized void resetWaitingTime() {
		this.waitingTime = 0;
		this.waitingTotal = 0;
	}

	private synchronized void setWaitingTime(long elapsed) {
		// if i'm here i have been in wait
		long avg = (this.waitingTime*this.waitingTotal)+elapsed;		// weighted average
		this.waitingTotal++;	// number of threads that have been in waiting, used to compute the avg
		this.waitingTime = avg / this.waitingTotal;	// save the new avg in millisec
	}

	/* ################################# Functions ################################# */

	// Handle the park join
	public boolean enter(String plate, String brand) throws Exception {
		synchronized(this){
			long start = 0;			// to take time for avg waiting time
            while (park.size() >= N_available && !isStopped()){	// while park si full and the server is on
            	if (start == 0){	// if i'm going to wait, take actual time and count me as waitingThread
            		start = System.currentTimeMillis();
            		this.waitingThreads++;	// add when thread is here, while he is in wait the value is incremented
            	}
            	wait();
            }
            if (start != 0)	{		// if i have been in waiting, compute waiting time
            	setWaitingTime((System.currentTimeMillis() - start)/100);	// divided by 100 to have precision of 1/10 of seconds
            	this.waitingThreads--;	// sub when he go on
            }
        }
        if(isStopped())	return false;
        // we are here if server is on and we can join the park slot
        park.putIfAbsent(plate, brand);
        return true;
	}

	// Handle the park exit
	public boolean exit(String plate) throws Exception {
		if(!isStopped()){
			park.remove(plate);
	        synchronized(this){
	            notify();		// notify if someone exit
	        }
	        return true;
    	}
    	return false;
	}

	// Park to JSON
	public String toJson() {
		Gson gson = new Gson(); 
		String json = gson.toJson(park); 
		return json;
	}

	// Stop the server
	public void stop() {
		synchronized(this){
			this.isStopped = true;
	        notifyAll();				// notify the server is closing
	    }
		try {
			this.serverSocket.close();	// close the socket that handle the requests
			this.threadPool.shutdown();	// end the current trasmission
			System.out.println("\n############################\nStopping Server.\n\nServer is being stop.\nReject latest request waiting for response cause 'Closing Server'\n");
			
			// await termination for 30s
			if (!this.threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
       			this.threadPool.shutdownNow(); // cancel currently executing tasks
			}

       		System.out.println("\nServer Stopped.");
		} catch (Exception e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	/* ################################# Run ################################# */

	public void run() {		// executed only one time
		System.out.println("Server ON.");

		try {	// create the socket
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port", e);
		}

		// while the server is not shut down
		while (!isStopped()) {
			try {

				Socket clientSocket = null;		// new socket for client
				clientSocket = this.serverSocket.accept();	// wait until a request is found
				// create thread to execute the request with reference to this for enter and exit
				this.threadPool.execute(new WorkerRunnable(clientSocket, this));

			} catch (IOException e) {
				if (isStopped())	// if we are here because the server is being stop
					break;	
				throw new RuntimeException("Error accepting client connection", e);	// accept error
			}
		}
	}


	public static void main(String[] args){
		int port;
		int parkingSpots;
		int time;

		// Read config file for server post, number of parking spot and TTL of server.
		// Config file is 3 rows, every rows has name:data. I only need data, order is important.
		try{
			BufferedReader bufferreader = new BufferedReader(new FileReader("SmartParking/Server/Server.config"));

			// Read 3 input from file
			String line;
			
			// Take server port
	        if( (line = bufferreader.readLine()) == null) {
	        	System.out.println("Bad config");
	        	bufferreader.close();
                return;
	        }
	        port = Integer.parseInt(line.split(":")[1]);
	        
	        // Take number of parking spot
	        if( (line = bufferreader.readLine()) == null) {
	        	System.out.println("Bad config");
	        	bufferreader.close();
                return;
	        }
	        parkingSpots = Integer.parseInt(line.split(":")[1]);

	        // Take TTL in seconds
	        if( (line = bufferreader.readLine()) == null) {
	        	System.out.println("Bad config");
                bufferreader.close();
	        	return;
	        }
	        time = Integer.parseInt(line.split(":")[1]);
            bufferreader.close();
			
            // Print info
            System.out.println("\n#######################################################\nServer running on localhost:" + port + " with " + parkingSpots + " parking lots.\nIt will be open for " + time + " seconds\n#######################################################\n");

			// Create server and start execution
	        ThreadPooledServer server = new ThreadPooledServer(port, parkingSpots);	// insert here (the port and) the number of parking spot
			new Thread(server).start();	// start the server

			Thread.sleep(time * 1000);	// seconds until stop (seconds)
			
			server.stop();				// end

		} catch (Exception e) {
			System.out.println("Missing config file");
		}
	}
}