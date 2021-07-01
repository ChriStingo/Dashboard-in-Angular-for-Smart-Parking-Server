import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class RequestService {

    // Send the http GET request to server on port, return response (data or STOP)
    public sendRequest(server: string, port: string, s: string) {
        return new Promise((resolve) => {   // Used for async AJAX, function resolve take output
            try {
                var xhr = new XMLHttpRequest();
                xhr.open('GET', 'http://' + server + ':' + port + '/' + s, true); // sync request
                xhr.onerror = function () { resolve("STOP"); }; // server closed
                xhr.onreadystatechange = function () {
                    if (this.readyState == 4) {
                        if (this.status == 200) resolve(this.responseText);
                        else resolve("STOP");   // server closed
                    }
                };
                xhr.timeout = 2000; // Set timeout to 2 seconds
                xhr.ontimeout = function () { resolve("STOP"); }; // server closed
                xhr.send(null);
            } catch (e) {
                // server already closed, STOP is keyword sent by server if he is going to close, no difference for caller
                resolve("STOP");
            }
        });
    }

    constructor() { }

}
