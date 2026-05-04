import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-boleta-print',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './boleta-print.html',
  styleUrls: ['./boleta-print.css']
})
export class BoletaPrintComponent {

  @Input() boleta: any;

  getRut(): string {
    return this.boleta?.empresa?.rut || '';
  }

  formatFecha(fecha: any): string {
    return new Date(fecha).toLocaleString();
  }
}