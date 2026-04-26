import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Location } from '@angular/common';

import { VentasService } from '../../../services/ventas/ventas';

@Component({
  selector: 'app-ventas-historial',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatCardModule
  ],
  templateUrl: './ventas-historial.html',
  styleUrl: './ventas-historial.css',
})
export class VentasHistorial implements OnInit {

  estado: 'hub' | 'list' = 'hub';

  ventas: any[] = [];
  cargando = false;

  constructor(
    private router: Router,
    private ventasService: VentasService,
    private location: Location
  ) {}

  ngOnInit() {}

  nuevaVenta() {
    this.router.navigate(['/ventas/form']);
  }

  verHistorial() {
    this.estado = 'list';
    this.cargarVentas();
  }

volver() {
  if (this.estado === 'list') {
    this.estado = 'hub';
    return;
  }

  this.location.back(); // 🔥 real "go back"
}

  cargarVentas() {
    this.cargando = true;

    this.ventasService.getMovimientos().subscribe({
      next: (data: any[]) => {

        // 🔥 Filter only SALIDA
        this.ventas = (data || [])
          .filter(v => v.movimientoTipo === 'SALIDA')
          .map(v => ({
            ...v,
            expandido: false
          }));

        this.cargando = false;
      },
      error: () => {
        this.ventas = [];
        this.cargando = false;
        alert('Error cargando ventas');
      }
    });
  }

  toggleExpand(venta: any) {
    venta.expandido = !venta.expandido;
  }
}