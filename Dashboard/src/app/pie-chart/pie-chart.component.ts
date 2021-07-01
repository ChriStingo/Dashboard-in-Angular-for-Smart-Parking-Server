import { Component, Input } from '@angular/core';
import { ChartType, ChartOptions } from 'chart.js';
import { SingleDataSet, Label, monkeyPatchChartJsLegend, monkeyPatchChartJsTooltip } from 'ng2-charts';

@Component({
    selector: 'app-pie-chart',
    templateUrl: './pie-chart.component.html',
    styleUrls: ['./pie-chart.component.css']
})

export class PieChartComponent {

    public pieChartOptions: ChartOptions = {
        responsive: true,
    };
    public pieChartLabels: Label[] = [['Taken'], ['Empty']];
    @Input() public pieChartData: SingleDataSet = [];
    public pieChartType: ChartType = 'pie';
    public pieChartLegend = true;
    public pieChartPlugins = [];
    public colors = [
        {
          backgroundColor: [
            'rgba(255, 0, 0, 0.6)',
            'rgba(0, 189, 0, 0.7)'
          ]
        }
      ];

    constructor() {
        monkeyPatchChartJsTooltip();
        monkeyPatchChartJsLegend();
    }

}