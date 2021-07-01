package SmartParking.Server;

/*
	Author: Christian Stingone

	This class corresponds to the threads that, given a request, satisfy it and send a response to the client.
	The ability to view error logs in the error.log file has also been implemented.
	An example to view an error log would be to run the "nmap" command
	on the server address or open a browser tab on localhost: port.
	The terminal printout relating to "Request processed sent:" prints the date and time of the request that
	and just been processed, so it could also be an "old" request that was pending.
*/

import java.net.Socket;
import java.io.*;
import java.time.format.DateTimeFormatter;	// time for logfile  
import java.time.LocalDateTime; 			// time for logfile

// handle the request from client
public class WorkerRunnable implements Runnable {

	protected Socket clientSocket;							// socket to answer the client
	protected ThreadPooledServer park;						// server of park
	protected static String error_filename = "error.log";	// file for log of errors


	public WorkerRunnable(Socket clientSocket, ThreadPooledServer park) {
		this.clientSocket = clientSocket;
		this.park = park;
	}

	/* ################################# Response ################################# */

	// request from dashboard
	private String dashboardResponse(String request) throws Exception{
		if (request.contains("info")) {		// if request is the first one, it is asking for park size
			int parks = park.getN_available();
			return ("HTTP/1.1 200 OK\nContent-Length: " + String.valueOf(parks).length() + "\nContent-Type: text; charset=utf-8\nAccess-Control-Allow-Origin: *\n\n" + parks + "\n"); 
		}
		if (request.contains("park")) {		// if request is asking for park, queue data and avg waiting time
			if (!park.isStopped()) {
				String json = park.toJson();				// JSON of park
				int inWait = park.getWaitingThreads();		// number of threads in wait
				int waitingTime = park.getWaitingTime();	// avg waiting time
				park.resetWaitingTime();					// reset avg waiting time
				return ("HTTP/1.1 200 OK\nContent-Length: " + (json.length() + 1 + String.valueOf(inWait).length() + 1 + String.valueOf(waitingTime).length())+ "\nContent-Type: json; charset=utf-8\nAccess-Control-Allow-Origin: *\n\n" + json + ";" + inWait + ";" + waitingTime + "\n"); 	
			}
			// if i'm here the server is stopped
			return ("HTTP/1.1 200 OK\nContent-Length: 4 \nContent-Type: text; charset=utf-8\nAccess-Control-Allow-Origin: *\n\nSTOP\n"); 	
		}
		// no other request accepted
		throw new Exception("Bad HTTP Request");	
	}

	// request from car
	private String parkingResponse(String request) throws Exception{
		String[] parts = request.split(",");

		/* REQUEST and PARTS format
		*	Request: type,brand,plate,Date Time (with space between)
		*
		*	Parts of request:
		*		parts[0] = type
		*		parts[1] = brand
		*		parts[2] = plate
		*		parts[3] = Date Time (with space between)
		*/

		if(parts[0].equals("0")) {	// enter the parking spot
			if(park.enter(parts[2], parts[1]))
				return "OK - Entered";
		} else {					// exit the parking spot
			if(park.exit(parts[2]))	
				return "OK - Exited";
		}
		// if enter or exit return false
		return "FAIL - Park is closed";
	}

	// parse the request and create response string
	private String computeResponse(String request) throws Exception {	// throw exception for 'Bad request' too
		if(request.contains("GET")){	// check if request is from dashboard
			return dashboardResponse(request);
		}
		// else is a parking request
		return parkingResponse(request);
	}

	// take time from request
	private String takeTime(String request) {
		if(request.contains("GET")){	// check if request is from dashboard
			return "Dashboard request";
		}
		// request from car
		String[] parts = request.split(",");
		return parts[parts.length-1];
	}

	/* ################################# Run ################################# */
 
	public void run() {
		String request = "EmptyRequest";	// init at "EmptyRequest" for log purpouse
		try { 
			// take streams
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			OutputStream output = clientSocket.getOutputStream();

			// take request
			byte[] b = new byte[100];
			if(input.read(b) > 0)	// if don't read anything, leave "EmptyRequest" string
				request = new String(b);

			// compute and send the response
			output.write(computeResponse(request).getBytes());			

			// close streams
			output.close();
			input.close();
			clientSocket.close();	// no keep alive

			// prompt log
			System.out.println("Request processed sent: " + takeTime(request));
		} catch (Exception e) { 
			try{
				// Here we print in error logfile
				System.out.println("Request failed: check " + error_filename);

				// time for exception
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
   				LocalDateTime now = LocalDateTime.now();

   				// write (append) to file
   				FileWriter file = new FileWriter(error_filename, true);
				file.write(dtf.format(now) + " - Error with request: " + request + "\n");
				file.close();
				
			} catch(Exception exc){
				exc.printStackTrace();
			}
		}
	}
}
