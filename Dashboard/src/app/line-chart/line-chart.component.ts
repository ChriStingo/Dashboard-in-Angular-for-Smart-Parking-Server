import { Component, Input, } from '@angular/core';
import { ChartDataSets, ChartType } from 'chart.js';
import { Color, Label } from 'ng2-charts';

@Component({
    selector: 'app-line-chart',
    templateUrl: './line-chart.component.html',
    styleUrls: ['./line-chart.component.css']
})

export class LineChartComponent {

    @Input() public lineChartData: ChartDataSets[] = [];

    lineChartLabels: Label[] = ["1" ,"3" ,"5" ,"7" ,"9" ,"11" ,"13" ,"15" ,"17" ,"19" ,"21" ,"23" ,"25" ,"27" ,"29" ,"31" ,"33" ,"35" ,"37" ,"39" ,"41" ,"43" ,"45" ,"47" ,"49" ,"51" ,"53" ,"55" ,"57" ,"59"];
    lineChartOptions = {
        responsive: true,
        scales: {
            yAxes: [{
              ticks: {
                min: 0,
              }
            }]}
    };

    lineChartColors: Color[] = [
        {
            borderColor: 'black',
            backgroundColor: 'rgba(190,190,190,0.5)',
        },
    ];

    lineChartLegend = true;
    lineChartPlugins = [];
    lineChartType: ChartType = 'line';

}