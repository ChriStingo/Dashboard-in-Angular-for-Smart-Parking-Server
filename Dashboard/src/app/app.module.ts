import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { HttpClientModule } from '@angular/common/http';
import { PieChartComponent } from './pie-chart/pie-chart.component';
import { ChartsModule } from 'ng2-charts';
import { LineChartComponent } from './line-chart/line-chart.component';
import { RequestService } from './request-service/request.service';

@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        PieChartComponent,
        LineChartComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        FormsModule,
        HttpClientModule,
        ChartsModule
    ],
    providers: [RequestService],
    bootstrap: [AppComponent]
})
export class AppModule { }
