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

  constructor(private lugarService: LugarService) {}

  ngOnInit(): void {
    this.cargarLugares();
  }

  cargarLugares(): void {
    this.lugarService.getLugares().subscribe({
      next: (data: MovimientoLugar[]) => this.lugares = data,
      error: (err: any) => console.error('Error al cargar lugares', err)
    });
  }

  toggleEstado(lugar: MovimientoLugar): void {
    if (!lugar.movimientoLugarId) return;

    if (lugar.movimientoLugarActivo) {
      // Si está activo, llamamos al endpoint de soft-delete de Javier
      this.lugarService.desactivarLugar(lugar.movimientoLugarId).subscribe({
        next: () => this.cargarLugares(),
        error: (err: any) => console.error('Error al desactivar', err)
      });
    } else {
      // Si está inactivo y lo queremos reactivar, lo mandamos por el POST (actualizar) con el boolean en true
      const lugarReactivado: MovimientoLugar = { ...lugar, movimientoLugarActivo: true };
      this.lugarService.guardarLugar(lugarReactivado).subscribe({
        next: () => this.cargarLugares(),
        error: (err: any) => console.error('Error al reactivar', err)
      });
    }
  }
  borrarLugar(id: number | undefined): void {
    if (!id) return;
    
    // Ventana de confirmación
    const confirmar = window.confirm('¿Estás seguro de que quieres eliminar este lugar para siempre?');
    
    if (confirmar) {
      this.lugarService.eliminarLugar(id).subscribe({
        next: () => {
          alert('🗑️ Lugar eliminado con éxito');
          this.cargarLugares(); // Recargar la tabla automáticamente
        },
        error: (err: any) => {
          console.error('Error al eliminar', err);
          // si la BD rechaza el borrado es por la FK
          alert('❌ No se pudo eliminar. Es probable que este lugar ya tenga productos o movimientos asociados.');
        }
      });
    }
  }
}