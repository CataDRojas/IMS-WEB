import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Location } from '@angular/common';
import { RecepcionService } from '../../../services/recepcion/recepcion';

@Component({
  selector: 'app-recepcion-historial',
  standalone: true,
  imports: [CommonModule, MatToolbarModule, MatButtonModule, MatCardModule],
  templateUrl: './recepcion-historial.html',
  styleUrl: './recepcion-historial.css',
})
export class RecepcionHistorial implements OnInit {

  recepciones: any[] = [];
  cargando = false;

  constructor(
    private router: Router,
    private location: Location,
    private recepcionService: RecepcionService
  ) {}

  ngOnInit() { this.cargarHistorial(); }

  cargarHistorial() {
    this.cargando = true;
    this.recepcionService.obtenerTodosMovimientosEntrada().subscribe({
      next: (data: any[]) => {
        this.recepciones = (data || []).map(m => ({ ...m, expandido: false }));
        this.cargando = false;
      },
      error: () => { this.recepciones = []; this.cargando = false; alert('Error cargando historial'); }
    });
  }

  toggleExpand(rec: any) { rec.expandido = !rec.expandido; }

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

  calcularTotalUnidades(detalles: any[]): number {
    if (!detalles) return 0;
    return detalles.reduce((sum, d) =>
      sum + (d.movimientoDetalleCantidad * d.movimientoDetalleUnidadesPorPaquete), 0);
  }

  async anular(rec: any, event: Event) {
    event.stopPropagation();
    if (!confirm(`¿Anular la recepción "${rec.movimientoDescripcion}"? Esto revertirá el stock.`)) return;
    try {
      await this.recepcionService.anularMovimiento(rec.movimientoId).toPromise();
      alert('✅ Recepción anulada y stock revertido.');
      this.cargarHistorial();
    } catch (err: any) { alert('❌ Error al anular.'); }
  }

  async reactivar(rec: any, event: Event) {
    event.stopPropagation();
    if (!confirm(`¿Reactivar la recepción "${rec.movimientoDescripcion}"? Esto volverá a sumar el stock.`)) return;
    try {
      await this.recepcionService.reactivarMovimiento(rec.movimientoId).toPromise();
      alert('✅ Recepción reactivada.');
      this.cargarHistorial();
    } catch (err: any) { alert('❌ Error al reactivar.'); }
  }

  volver() { this.location.back(); }
}