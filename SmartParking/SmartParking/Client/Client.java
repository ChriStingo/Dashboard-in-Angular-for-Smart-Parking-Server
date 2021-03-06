package SmartParking.Client;

import java.net.InetSocketAddress;

/*
	Author: Christian Stingone

	This class corresponds to the client class, to make server operation visible
	we have implemented threads, each thread corresponds to a machine with its data and prints the connection / response status on the terminal.
	Each machine will make a connection to the server requesting first to enter and then to exit the parking lot
	a finite number of times (with a random delay, with a fixed minimum).
*/

import java.net.Socket;
import java.io.*;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime; 

public class Client extends Thread {
	// Connection data
	private String server_ip;
	private int port;
    private int timeout = 0;
	private Socket server;
	private DataInputStream input;
	private OutputStream output;

	// Car data
	private String plate;
	private String brand;
	private int simulations;

	// generate random plate
	private String randomPlate() {
		String tmp = "qwertyuiopasdfghjklzxcvbnm";
		String tmp_plate = "";
		for(int i = 0; i < 7; ++i){
			if(i < 2)
				tmp_plate += tmp.charAt((int)(Math.random() * tmp.length()));
			else if(i < 5)
				tmp_plate += (int)(Math.random()*10);
			else
				tmp_plate += tmp.charAt((int)(Math.random() * tmp.length()));
		}
		return tmp_plate;
	}

	// generate random brand
	private String randomBrand() {
		String [] brands = {"Alfa Romeo", "Aston Martin", "Audi", "Bentley", "BMW", "Bugatti", "Citroen", "Chevrolet", "Dacia", "Ferrari", "Fiat", "Ford", "Honda", "Hyundai", "Jaguar", "Jeep", "Kia", "Lamborghini", "Lancia", "Land Rover", "Lexus", "Lotus", "Maserati", "Mazda", "Mclaren", "Mercedes", "Mini", "Mitsubishi", "Nissan", "Opel", "Pagani", "Peugeot", "Porsche", "Renault", "Seat", "Skoda", "Smart", "Subaru", "Suzuki", "Tesla", "Toyota", "Volvo"};
		return brands[(int)(Math.random()*brands.length)];
	}

	public Client(String server_ip, int port, int simulations, int timeout) {
		this.server_ip = server_ip;
		this.port = port;
		this.simulations = simulations;
		// create random plate
		plate = randomPlate();
		// create random brand
		brand = randomBrand();
        this.timeout = timeout * 1000;
	}

	// init of socket and input/output streams, throw Exception if impossible to connect to server (server closed)
	private void init_socket() throws Exception {
		server = new Socket();

        // Max timeout for client socket in seconds (0 = infinity, our server end comunication)
        server.connect(new InetSocketAddress(server_ip, port), timeout);

		input = new DataInputStream(server.getInputStream());	// to retrieve the response from server
		output = server.getOutputStream();						// where we write to server
	}

	// create request with pre-established format
	private String formatRequest(int iteration) {
		// time
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
   		LocalDateTime now = LocalDateTime.now();  

		return ((iteration%2 == 0 ? "0" : "1") + "," + brand + "," + plate + "," + dtf.format(now));
		// Type,Brand,Plate,Date Hour (with space between)
		// Type = if iteration is even then 0 (enter), 1 (exit) otherwise
	}
	
	public void run() {
		try {
			for(int i = 0; i < simulations; i++) {			// number of simulated iteration of enter/exit 
				sleep((long)(Math.random()*9500)+500);		// add random sleep (0.5s to 10s)
				init_socket();								// new connection, if park is closed throw exception

				// send data to the server
				String request = formatRequest(i);
				output.write(request.getBytes()); 
				
				// retrieve response
				byte[] b = new byte[21];	// max length for server response (pre-established)
				input.read(b);
				String response = new String(b);

				// prompt log
				System.out.println(plate + " - " + response);
			}
			// end
			server.close();
		} catch (Exception e) {
			// errors or server closed
			System.out.println("Unable to establish communication with the server");
		}
	}

	public static void main(String[] args){
		String server_ip;
		int port;
		int threadNumber;
		int simulations;
        int timeout;

		// Read config file for server ip, port, number of threads and number of simulations
		// Config file is 4 rows, every rows has name:data. I only need data, order is important.
		try{
			BufferedReader bufferreader = new BufferedReader(new FileReader("SmartParking/Client/Client.config"));

			// Read 4 input from file
			String line;
			
			// Take server ip
	        if( (line = bufferreader.readLine()) == null) {
	        	System.out.println("Bad config");
	        	bufferreader.close();
                return;
	        }
	        server_ip = line.split(":")[1];
	        
	        // Take server port
	        if( (line = bufferreader.readLine()) == null) {
	        	System.out.println("Bad config");
	        	bufferreader.close();
                return;
	        }
	        port = Integer.parseInt(line.split(":")[1]);

	        // Take number of threads
	        if( (line = bufferreader.readLine()) == null) {
	        	System.out.println("Bad config");
	        	bufferreader.close();
                return;
	        }
	        threadNumber = Integer.parseInt(line.split(":")[1]);

	        // Take number of simulations
	        if( (line = bufferreader.readLine()) == null) {
	        	System.out.println("Bad config");
                bufferreader.close();
	        	return;
	        }
	        simulations = Integer.parseInt(line.split(":")[1]);

            // Take timeout time for socket
	        if( (line = bufferreader.readLine()) == null) {
	        	System.out.println("Bad config");
                bufferreader.close();
	        	return;
	        }
	        timeout = Integer.parseInt(line.split(":")[1]);
            bufferreader.close();

            // Print info
            System.out.println("\n#######################################################\nConnect with: " + server_ip + ":" + port + " with " + threadNumber + " cars.\nIt will run " + simulations + " simulations (enter/exit) before stop\n#######################################################\n");
			
	        // Create clients
			Client[] threads = new Client[threadNumber];
			
			// Start execution
			for(int i = 0; i < threadNumber; ++i) {
				threads[i] = new Client(server_ip, port, simulations, timeout);
				threads[i].start();
			}

		}catch(Exception e) {
			System.out.println("Missing config file");
		}
	}
}
