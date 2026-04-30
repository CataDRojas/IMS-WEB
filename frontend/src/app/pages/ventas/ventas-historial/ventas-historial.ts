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
  imports: [
    CommonModule,
    FormsModule,
    MatToolbarModule,
    MatButtonModule,
    MatCardModule
  ],
  templateUrl: './ventas-historial.html',
  styleUrl: './ventas-historial.css',
})
export class VentasHistorial implements OnInit, OnDestroy {

  estado: 'hub' | 'list' = 'hub';

  ventas: any[] = [];
  cargando = false;

  // =========================
  // PAGINATION
  // =========================
  paginaActual = 1;
  tamanoPagina = 10;

  totalPaginas = 0;
  totalItems = 0;

  // =========================
  // FILTERS
  // =========================
  filtros = {
    tipo: 'SALIDA',
    estado: '',
    usuario: '',
    desde: '',
    hasta: ''
  };

  // =========================
  // UX STREAMS
  // =========================
  private usuarioChange$ = new Subject<string>();
  private usuarioSub?: Subscription;
  private isLoadingRequest = false;

  constructor(
    private router: Router,
    private ventasService: VentasService,
    private location: Location
  ) {}

  ngOnInit() {

    this.usuarioSub = this.usuarioChange$
      .pipe(debounceTime(400))
      .subscribe(() => {
        this.aplicarFiltros();
      });
  }

  ngOnDestroy() {
    this.usuarioSub?.unsubscribe();
  }

  // =========================
  // NAVIGATION
  // =========================
  nuevaVenta() {
    this.router.navigate(['/ventas/form']);
  }

  verHistorial() {
    this.estado = 'list';
    this.paginaActual = 1;
    this.cargarVentas();
  }

volver() {
  if (this.estado === 'list') {
    this.estado = 'hub';
    return;
  }

  // HUB is treated as a root screen → always go home
  this.router.navigate(['/home']); // or whatever your real home route is
}

  // =========================
  // DATA LOAD
  // =========================
  cargarVentas() {

    if (this.isLoadingRequest) return;

    this.isLoadingRequest = true;
    this.cargando = true;

    const pageIndex = this.paginaActual - 1;

    this.ventasService.getMovimientosPaginados({
      tipo: this.filtros.tipo,
      estado: this.filtros.estado || undefined,
      usuario: this.filtros.usuario || undefined,
      desde: this.filtros.desde || undefined,
      hasta: this.filtros.hasta || undefined,
      page: pageIndex,
      size: this.tamanoPagina
    }).subscribe({
      next: (res: any) => {

        const data = res?.content ?? [];

this.ventas = data.map((v: any) => ({

  ...v,

  expandido: false,

  usuarioCreacion: v.movimientoUsuarioCreacion,
  fechaCreacion: v.movimientoFechaCreacion,

  usuarioModificacion: v.movimientoUsuarioModif,
  fechaModificacion: v.movimientoFechaModif,

  // =========================
  // 💰 UI MONEY NORMALIZATION
  // =========================

  movimientoTotal: Math.floor(v.movimientoTotal ?? 0),
  movimientoSubtotal: Math.floor(v.movimientoSubtotal ?? 0),
  movimientoDescuento: Math.floor(v.movimientoDescuento ?? 0),
  movimientoNeto: Math.floor(v.movimientoNeto ?? 0),
  // IVA INCLUDED IN RULE (NO DECIMALS ANYWHERE)
  movimientoIva: Math.floor(v.movimientoIva ?? 0),

  movimientoTotalFinal: Math.floor(v.movimientoTotalFinal ?? 0)
}));

        this.totalPaginas = res?.totalPages ?? 0;
        this.totalItems = res?.totalElements ?? 0;

        this.cargando = false;
        this.isLoadingRequest = false;
      },

      error: () => {
        this.ventas = [];
        this.totalPaginas = 0;
        this.totalItems = 0;

        this.cargando = false;
        this.isLoadingRequest = false;
      }
    });
  }

  // =========================
  // FILTER ACTIONS
  // =========================
  aplicarFiltros() {
    this.paginaActual = 1;
    this.cargarVentas();
  }

  limpiarFiltros() {
    this.filtros = {
      tipo: 'SALIDA',
      estado: '',
      usuario: '',
      desde: '',
      hasta: ''
    };

    this.paginaActual = 1;
    this.cargarVentas();
  }

  onUsuarioChange(value: string) {
    this.filtros.usuario = value;
    this.usuarioChange$.next(value);
  }

  // =========================
  // SORTING (FRONTEND ONLY)
  // =========================
  ordenarPor(field: string) {
    this.ventas = [...this.ventas].sort((a, b) => {

      const av = a[field];
      const bv = b[field];

      if (av == null) return 1;
      if (bv == null) return -1;

      if (typeof av === 'number') return bv - av;

      return String(av).localeCompare(String(bv));
    });
  }

  // =========================
  // PAGINATION CONTROL
  // =========================
  cambiarPagina(delta: number) {

    const nueva = this.paginaActual + delta;

    if (nueva < 1 || nueva > this.totalPaginas) return;

    this.paginaActual = nueva;
    this.cargarVentas();
  }

  // =========================
  // UI
  // =========================
  toggleExpand(venta: any) {
    venta.expandido = !venta.expandido;
  }
}