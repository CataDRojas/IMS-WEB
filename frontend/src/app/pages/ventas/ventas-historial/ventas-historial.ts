import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Location } from '@angular/common';
import { VentasService } from '../../../services/ventas/ventas';
import { Subject, debounceTime, Subscription } from 'rxjs';

@Component({
  selector: 'app-ventas-historial',
  standalone: true,
  imports: [CommonModule, FormsModule, MatToolbarModule, MatButtonModule, MatCardModule],
  templateUrl: './ventas-historial.html',
  styleUrl: './ventas-historial.css',
})
export class VentasHistorial implements OnInit, OnDestroy {

  movimientos: any[] = [];
  cargando = false;

  paginaActual = 1;
  tamanoPagina = 10;
  totalPaginas = 0;
  totalItems = 0;

  filtros = {
    tipo: '',
    estado: '',
    usuario: '',
    desde: '',
    hasta: ''
  };

  private usuarioChange$ = new Subject<string>();
  private usuarioSub?: Subscription;
  private isLoadingRequest = false;

  permisos: string[] = [];

  constructor(
    private router: Router,
    private ventasService: VentasService,
    private location: Location
  ) {}

  ngOnInit() {
    const permisosRaw = localStorage.getItem('permisos_ims');
    this.permisos = permisosRaw ? JSON.parse(permisosRaw) : [];

    this.usuarioSub = this.usuarioChange$
      .pipe(debounceTime(400))
      .subscribe(() => this.aplicarFiltros());

    this.cargarMovimientos();
  }

  ngOnDestroy() { this.usuarioSub?.unsubscribe(); }

  tienePermiso(p: string): boolean {
    return this.permisos.includes(p);
  }

  tiposDisponibles(): string[] {
    const tipos: string[] = [];
    if (this.tienePermiso('VENTA_READ') || this.tienePermiso('VENTA_MANAGE')) {
      tipos.push('SALIDA');
    }
    if (this.tienePermiso('INVENTARIO_READ') || this.tienePermiso('INVENTARIO_MANAGE')) {
      tipos.push('ENTRADA');
      tipos.push('AJUSTE');
    }
    return tipos;
  }

  getTipoLabel(tipo: string): string {
    switch (tipo) {
      case 'SALIDA': return '🛒 Venta';
      case 'ENTRADA': return '📥 Recepción';
      case 'AJUSTE': return '📋 Inventario';
      default: return tipo;
    }
  }

  getTipoColor(tipo: string): string {
    switch (tipo) {
      case 'SALIDA': return '#1976d2';
      case 'ENTRADA': return '#388e3c';
      case 'AJUSTE': return '#f57c00';
      default: return '#999';
    }
  }

  getEstadoColor(estado: string): string {
    switch (estado) {
      case 'CONFIRMADO': return '#4caf50';
      case 'PENDIENTE': return '#ff9800';
      case 'ANULADO': return '#f44336';
      default: return '#999';
    }
  }

  volver() { this.router.navigate(['/home']); }

  cargarMovimientos() {
    if (this.isLoadingRequest) return;
    this.isLoadingRequest = true;
    this.cargando = true;

    this.ventasService.getMovimientosPaginados({
      tipo: this.filtros.tipo || undefined,
      estado: this.filtros.estado || undefined,
      usuario: this.filtros.usuario || undefined,
      desde: this.filtros.desde || undefined,
      hasta: this.filtros.hasta || undefined,
      page: this.paginaActual - 1,
      size: this.tamanoPagina
    }).subscribe({
      next: (res: any) => {
        const data = res?.content ?? [];
        this.movimientos = data
          .filter((m: any) => this.tiposDisponibles().includes(m.movimientoTipo))
          .map((m: any) => ({ ...m, expandido: false }));
        this.totalPaginas = res?.totalPages ?? 0;
        this.totalItems = res?.totalElements ?? 0;
        this.cargando = false;
        this.isLoadingRequest = false;
      },
      error: () => {
        this.movimientos = [];
        this.totalPaginas = 0;
        this.totalItems = 0;
        this.cargando = false;
        this.isLoadingRequest = false;
      }
    });
  }

  aplicarFiltros() { this.paginaActual = 1; this.cargarMovimientos(); }

  limpiarFiltros() {
    this.filtros = { tipo: '', estado: '', usuario: '', desde: '', hasta: '' };
    this.paginaActual = 1;
    this.cargarMovimientos();
  }

  onUsuarioChange(value: string) {
    this.filtros.usuario = value;
    this.usuarioChange$.next(value);
  }

  cambiarPagina(delta: number) {
    const nueva = this.paginaActual + delta;
    if (nueva < 1 || nueva > this.totalPaginas) return;
    this.paginaActual = nueva;
    this.cargarMovimientos();
  }

  toggleExpand(m: any) { m.expandido = !m.expandido; }

  calcularTotalUnidades(detalles: any[]): number {
    if (!detalles) return 0;
    return detalles.reduce((sum, d) =>
      sum + (d.movimientoDetalleCantidad * d.movimientoDetalleUnidadesPorPaquete), 0);
  }

  async anular(m: any, event: Event) {
    event.stopPropagation();
    if (!confirm(`¿Anular la recepción "${m.movimientoDescripcion}"? Esto revertirá el stock.`)) return;
    try {
      await this.ventasService.anularMovimiento(m.movimientoId).toPromise();
      alert('✅ Recepción anulada.');
      this.cargarMovimientos();
    } catch (err) {
      alert('❌ Error al anular.');
    }
  }

  async reactivar(m: any, event: Event) {
    event.stopPropagation();
    if (!confirm(`¿Reactivar la recepción "${m.movimientoDescripcion}"?`)) return;
    try {
      await this.ventasService.reactivarMovimiento(m.movimientoId).toPromise();
      alert('✅ Recepción reactivada.');
      this.cargarMovimientos();
    } catch (err) {
      alert('❌ Error al reactivar.');
    }
  }
  exportarExcel() {
  this.ventasService.exportarReporte({
    tipo: this.filtros.tipo || undefined,
    estado: this.filtros.estado || undefined,
    usuario: this.filtros.usuario || undefined,
    desde: this.filtros.desde || undefined,
    hasta: this.filtros.hasta || undefined,
  }).subscribe({
    next: (blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Reporte_IMS_${new Date().toISOString().slice(0,10)}.xlsx`;
      a.click();
      window.URL.revokeObjectURL(url);
    },
    error: () => alert('❌ Error al exportar el reporte.')
  });
}
}