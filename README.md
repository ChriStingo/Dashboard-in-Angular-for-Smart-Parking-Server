# Dashboard in Angular for Smart Parking Server
This is my bachelor degree thesis about "concurrent programming and distributed algorithms".

### Dashboard
Here you can find the Angular application. This Dashboard send a http request to the server and parse data and display them with some cool charts.
Check the README inside the folder for instructions.

### SmartParking
Here you can find the Java Server and Client architecture. The Client simulates cars with plates and brands. Every car is a thread. Every thread, with random delay, asks to the server to enter or exit the parking lot.
The Server accept every request, create a thread to answer and try to satisfy client request. If it's not possible puts him on hold until another car leave the parking lot.
Check the README inside the folder for instructions.

All the platform is distributed, you can change che ip and port for every component (Client, Server, Dashboard) and run them on different machines.
