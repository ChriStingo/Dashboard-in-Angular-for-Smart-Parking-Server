# Dashboard in Angular for Smart Parking Server
This is my bachelor degree thesis about "concurrent programming and distributed algorithms" made in 2021.
The dashbaord displays the status of the parking with some informations about queue time and fill percentage.

![execution](https://user-images.githubusercontent.com/62427405/124155268-9aae5d00-da96-11eb-82a9-0f18ccb296b0.png)

### Dashboard
Here you can find the Angular application. This Dashboard sends an http request to the server, parses data and displays them with some cool charts and animations.
![dashboard-server](https://user-images.githubusercontent.com/62427405/124153899-3dfe7280-da95-11eb-8b5a-ac4b94620256.png)

Check the README inside the folder for instructions.

### SmartParking
Here you can find the Client and Server architecture made in Java. The Client simulates cars with plates and brands. Every car is a thread. Every thread, with random delay, asks to the server to enter or exit the parking lot.
The Server accepts every request, creates a thread to answer and try to satisfy client requests. If it's not possible than puts him on hold until another car leaves the parking lot.
![Client-Server](https://user-images.githubusercontent.com/62427405/124153436-b7499580-da94-11eb-96cb-27ba9a9cbebc.png)

Check the README inside the folder for instructions.

-----
**The entire platform is distributed, you can change the ip and port for every component (Client, Server, Dashboard) and run them on different machines.**
