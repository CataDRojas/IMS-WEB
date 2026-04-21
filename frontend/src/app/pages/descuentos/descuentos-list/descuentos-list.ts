import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { DescuentosService, Descuento } from '../../../services/descuento/descuento';

@Component({
  selector: 'app-descuentos-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './descuentos-list.html'
})
export class DescuentosListComponent implements OnInit {

  descuentos: Descuento[] = [];
  loading = false;

  constructor(
    private service: DescuentosService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  // =========================
  // DATA LOAD
  // =========================
  load(): void {

    this.loading = true;

    this.service.getAll().subscribe({
      next: (data) => {
        this.descuentos = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  // =========================
  // NAVIGATION
  // =========================
  create(): void {
    this.router.navigate(['/descuentos/new']);
  }

  edit(id: number): void {
    if (!id) return;
    this.router.navigate(['/descuentos/edit', id]);
  }

  // =========================
  // DELETE
  // =========================
  delete(id: number): void {

    if (!id) return;
    if (!confirm('Eliminar descuento?')) return;

    this.service.delete(id).subscribe({
      next: () => this.load(),
      error: () => {
        // optional UI feedback later
      }
    });
  }

  // =========================
  // UI HELPERS (IMPORTANT NEW ADDITION)
  // =========================
  getTipoLabel(tipo: string): string {
    switch (tipo) {
      case 'FLAT': return 'Monto fijo';
      case 'PORCENTAJE': return 'Porcentaje';
      case 'MULTIPLICATIVO': return 'Multiplicador';
      default: return tipo;
    }
  }

  getValorDisplay(descuento: Descuento): string {
    if (descuento.descuentoTipo === 'PORCENTAJE') {
      return `${descuento.descuentoValor}%`;
    }

    if (descuento.descuentoTipo === 'FLAT') {
      return `$${descuento.descuentoValor}`;
    }

    return `x${descuento.descuentoValor}`;
  }

  goHome() {
    this.router.navigate(['/home']);
  }
}