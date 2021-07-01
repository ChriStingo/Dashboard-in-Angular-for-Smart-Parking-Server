import { Component, OnInit } from '@angular/core';
import { ChartDataSets } from 'chart.js';
import { SingleDataSet } from 'ng2-charts';
import { RequestService } from '../request-service/request.service';

// Used for AJAX
declare var xhr: XMLHttpRequest;

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css'],
})

export class DashboardComponent implements OnInit {
    // Connection data
    connection: boolean;        // if there is/was at least one connection
    connectionStatus: string;   // Output data
    reconnect: boolean;         // if connecton ends
    server: string;             // server address
    port: string;               // server port

    // Charts data
    pieData: SingleDataSet;     // @Input in pie-data-component
    lineData: ChartDataSets[];  // @Input in line-chart-component

    // Parking data
    Parks: string[];            // Output data, plate for the parking spot
    Brands: string[];           // Output data, brands for parking spot
    parkingNumber: string;      // Output data
    queueNumber: string;        // Output data
    closed: boolean;            // server status
    animations: string[];       // Output data, for css

    // Close connection, changing Output data and showing pop-up
    private close() {
        this.connectionStatus = 'Server Closed';
        this.queueNumber = '0';
        this.closed = true;     // alert for user
        this.reconnect = true;  // display button
    }

    // Prepare output to display, remove old elements, add new elements but not changing others
    private settingOutput(tmpParks: string[], tmpBrands: string[]) {
        // output arrays
        let newParks = new Array(+this.parkingNumber);
        let newBrands = new Array(+this.parkingNumber);
        this.animations = [];

        // find unchanged park spot -> O(n + O(indexOf))
        for (let i = 0; i < tmpParks.length; i++) {
            let index = this.Parks.indexOf(tmpParks[i]);    // index of new car inside old Parks
            if (index != -1) {                              // if was there
                newParks[index] = tmpParks[i];              // leave it in the same spot
                newBrands[index] = tmpBrands[i];            // every car -> its brand
                tmpParks.splice(i, 1);                      // remove element from tmp array
                tmpBrands.splice(i, 1);
                i--;
            }
        }

        // insert new cars -> cost is inverse propotional of the overlying function, worst case O((n*(n+1))/2) = O(n*n)
        for (let i = 0; i < tmpParks.length; i++) {             // for any other car
            for (let j = i; j < +this.parkingNumber; j++) {     // find empty spot
                if (typeof newParks[j] === "undefined") {       // add new car and brand
                    newParks[j] = tmpParks[i];
                    newBrands[j] = tmpBrands[i];
                    this.animations[j] = "animation";           // configure animation for this spot
                    break;                                      // go to next car
                }
            }
        }

        // output
        this.Parks = newParks;
        this.Brands = newBrands;
    }

    // Polling to server retrieving data
    private async retrieveData() {
        do {
            // Request to server to retrieve Parks, queueNumber and AvgQueueTime. Separate by ;
            let response = (""+(await this.requestService.sendRequest(this.server, this.port, 'park').then())).split(";");     // Cast to string the type unknown from Promise and then apply split function over ;
            if (response[0] == "STOP") {                    // server stopped
                this.close();
                continue;
            }

            // update parking data
            let tmpParks: string[] = Object.keys(JSON.parse(response[0].toUpperCase()));
            let carsIn = tmpParks.length;                   // tmpParks will change with this.settingOutput() function
            this.queueNumber = response[1];
            this.settingOutput(tmpParks, Object.values(JSON.parse(response[0])));   // tmpParks and tmpBrands


            // Charts data
            this.pieData = [
                carsIn,
                +this.parkingNumber - carsIn
            ];
            if (this.lineData[0].data?.length == 29)        // if line chart is full, empty it
                this.lineData[0].data = [];
            this.lineData[0].data?.push(+response[2] / 10);

            // polling delay
            await new Promise((f) => setTimeout(f, 2000));  // time in millisec.
        } while (!this.closed);     // while server is open
    }

    // Main function, called by Connect button
    async connect() {
        let infoData: string = await this.requestService.sendRequest(this.server, this.port, 'info').then();   // wait for response
        if (infoData != "STOP") {    // server is not closed
            this.parkingNumber = infoData;
            this.Parks = new Array(+this.parkingNumber);
            this.connection = true;
            this.reconnect = false;
            this.closed = false;
            this.connectionStatus = 'Connected';
            this.retrieveData();    // start polling to retrieve data
        } else
            this.close();           // server already closed
    }

    // Contructor, called when whe dashboard is open
    constructor(private requestService: RequestService) {       // RequestService is a service to send the request to the server and retrieve data
        // Connection data
        this.connection = false;
        this.connectionStatus = 'Not connected';
        this.reconnect = false;
        this.server = "127.0.0.1";
        this.port = "8080";

        // Charts data
        this.pieData = [];
        this.lineData = [{ data: [0], label: 'Waiting time (second)' }];

        // Parking data
        this.Parks = [];
        this.Brands = [];
        this.animations = [];
        this.parkingNumber = 'Undefined';
        this.queueNumber = '0';
        this.closed = false;
    }

    ngOnInit() { }
}
