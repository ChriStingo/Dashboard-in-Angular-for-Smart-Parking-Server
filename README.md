# Dashboard in Angular for Smart Parking Server
This is my bachelor degree thesis about "concurrent programming and distributed algorithms".
The dashbaord displays che status of the car park with some informations abount the queue time and the and the fill percentage.
![execution](https://user-images.githubusercontent.com/62427405/124155268-9aae5d00-da96-11eb-82a9-0f18ccb296b0.png)


### Dashboard
Here you can find the Angular application. This Dashboard sends an http request to the server and parse data and display them with some cool charts and animations.
![dashboard-server](https://user-images.githubusercontent.com/62427405/124153899-3dfe7280-da95-11eb-8b5a-ac4b94620256.png)

Check the README inside the folder for instructions.

### SmartParking
Here you can find the Java Server and Client architecture. The Client simulates cars with plates and brands. Every car is a thread. Every thread, with random delay, asks to the server to enter or exit the parking lot.
The Server accept every request, create a thread to answer and try to satisfy client request. If it's not possible puts him on hold until another car leave the parking lot.
![Client-Server](https://user-images.githubusercontent.com/62427405/124153436-b7499580-da94-11eb-96cb-27ba9a9cbebc.png)

Check the README inside the folder for instructions.

All the platform is distributed, you can change che ip and port for every component (Client, Server, Dashboard) and run them on different machines.
