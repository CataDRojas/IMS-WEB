import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatToolbar } from '@angular/material/toolbar';
import { Location } from '@angular/common';

@Component({
  selector: 'app-inventario-home',
  standalone: true,
  templateUrl: './inventario-home.html',
  styleUrls: ['./inventario-home.css'],
  imports: [MatCardModule, MatToolbar]
})
export class InventarioHomeComponent {

  constructor(private router: Router, private location: Location) {}

  goToNew() {
    this.router.navigate(['/inventario/nuevo']);
  }

  goToHistory() {
    this.router.navigate(['/inventario/historial']);
  }

  volver() {
    this.location.back();
  }
}