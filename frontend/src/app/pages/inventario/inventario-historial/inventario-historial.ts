import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { Location } from '@angular/common';
import { InventarioService } from '../../../services/inventario/inventario';

@Component({
  selector: 'app-inventario-historial',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule
  ],
  templateUrl: './inventario-historial.html',
  styleUrl: './inventario-historial.css',
})
export class InventarioHistorial implements OnInit {

  inventarios: any[] = [];
  cargando = false;

  constructor(
    private router: Router,
    private location: Location,
    private inventarioService: InventarioService
  ) {}

  ngOnInit() {
    this.cargarHistorial();
  }

  cargarHistorial() {
    this.cargando = true;
    this.inventarioService.obtenerTodosMovimientosEntrada().subscribe({
      next: (data: any[]) => {
        this.inventarios = (data || []).map(m => ({
          ...m,
          expandido: false
        }));
        this.cargando = false;
      },
      error: () => {
        this.inventarios = [];
        this.cargando = false;
        alert('Error cargando historial');
      }
    });
  }

  toggleExpand(inv: any) {
    inv.expandido = !inv.expandido;
  }

  getEstadoColor(estado: string): string {
    switch (estado) {
      case 'CONFIRMADO': return '#4caf50';
      case 'PENDIENTE': return '#ff9800';
      case 'ANULADO': return '#f44336';
      default: return '#999';
    }
  }

  getEstadoEmoji(estado: string): string {
    switch (estado) {
      case 'CONFIRMADO': return '✅';
      case 'PENDIENTE': return '⏳';
      case 'ANULADO': return '❌';
      default: return '❓';
    }
  }

  async anular(inv: any, event: Event) {
    event.stopPropagation();

    if (inv.movimientoEstado === 'ANULADO') {
      alert('Este inventario ya está anulado.');
      return;
    }

    if (inv.movimientoEstado === 'PENDIENTE') {
      alert('No se puede anular un inventario pendiente. Elimínalo desde el módulo de inventario.');
      return;
    }

    if (!confirm(`¿Anular el inventario "${inv.movimientoDescripcion}"? Esto revertirá el stock.`)) return;

    try {
      await this.inventarioService.anularMovimiento(inv.movimientoId).toPromise();
      alert('✅ Inventario anulado y stock revertido.');
      this.cargarHistorial();
    } catch (err: any) {
      console.error('Error anulando:', err);
      alert('❌ Error al anular. Revisa la consola.');
    }
  }

  volver() {
    this.location.back();
  }

    async reactivar(inv: any, event: Event) {
    event.stopPropagation();

    if (!confirm(`¿Reactivar el inventario "${inv.movimientoDescripcion}"? Esto volverá a sumar el stock.`)) return;

    try {
      await this.inventarioService.reactivarMovimiento(inv.movimientoId).toPromise();
      alert('✅ Inventario reactivado y stock aplicado nuevamente.');
      this.cargarHistorial();
    } catch (err: any) {
      console.error('Error reactivando:', err);
      alert('❌ Error al reactivar.');
    }
  }

  calcularTotalUnidades(detalles: any[]): number {
    if (!detalles) return 0;
    return detalles.reduce((sum, d) =>
      sum + (d.movimientoDetalleCantidad * d.movimientoDetalleUnidadesPorPaquete), 0);
  }
}