import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LugarService, MovimientoLugar } from '../../../services/lugar/lugar';

@Component({
  selector: 'app-lugares-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './lugares-list.html'
})
export class LugaresList implements OnInit {

  lugares: MovimientoLugar[] = [];
  cargando = false;

  constructor(private lugarService: LugarService) {}

  ngOnInit(): void {
    this.cargarLugares();
  }

  // =========================
  // LOAD DATA
  // =========================
  cargarLugares(): void {
    this.cargando = true;

    this.lugarService.getLugares().subscribe({
      next: (data: MovimientoLugar[]) => {
        this.lugares = data;
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar lugares', err);
        this.lugares = [];
        this.cargando = false;
      }
    });
  }

  // =========================
  // TOGGLE STATE (NOW FULLY SYMMETRIC)
  // =========================
  toggleEstado(lugar: MovimientoLugar): void {
    if (!lugar.movimientoLugarId) return;

    this.lugarService.desactivarLugar(lugar.movimientoLugarId).subscribe({
      next: () => {
        // reload to sync state from backend
        this.cargarLugares();
      },
      error: (err: any) => {
        console.error('Error al cambiar estado', err);
      }
    });
  }

  // =========================
  // DELETE
  // =========================
  borrarLugar(id: number | undefined): void {
    if (!id) return;

    const confirmar = window.confirm(
      '¿Estás seguro de que quieres eliminar este lugar para siempre?'
    );

    if (!confirmar) return;

    this.lugarService.eliminarLugar(id).subscribe({
      next: () => {
        alert('🗑️ Lugar eliminado con éxito');
        this.cargarLugares();
      },
      error: (err: any) => {
        console.error('Error al eliminar', err);
        alert('❌ No se pudo eliminar. Probablemente tiene relaciones activas.');
      }
    });
  }
}