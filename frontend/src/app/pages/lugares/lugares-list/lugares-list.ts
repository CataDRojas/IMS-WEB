import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { LugarService, MovimientoLugar } from '../../../services/lugar/lugar';

@Component({
  selector: 'app-lugares',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule, MatExpansionModule],
  templateUrl: './lugares-list.html',
  styleUrls: ['./lugares-list.css'] // Recuerda que puede heredar de productos.css
})
export class LugaresComponent implements OnInit {
  lugares: MovimientoLugar[] = [];
  mostrarFormulario = false;
  lugarForm: FormGroup;
  lugarActual: Partial<MovimientoLugar> = {};
  cargando = false;

  constructor(
    private fb: FormBuilder,
    private lugarService: LugarService
  ) {
this.lugarForm = this.fb.group({
  movimientoLugarDescripcion: ['', Validators.required],
  movimientoLugarActivo: [true],

  movimientoLugarPrioridad: [false]
});
  }

  ngOnInit(): void {
    this.cargarLugares();
  }

  // =========================
  // LOAD DATA
  // =========================
  cargarLugares(): void {
    this.cargando = true;

    this.lugarService.getLugares().subscribe({
      next: (data) => this.lugares = data,
      error: (err) => console.error('Error al cargar lugares', err)
    });
  }

  abrirNuevo(): void {
    this.lugarActual = {};
    this.lugarForm.reset({ movimientoLugarActivo: true });
    this.mostrarFormulario = true;
  }

  editar(lugar: MovimientoLugar): void {
    this.lugarActual = { ...lugar };
  this.lugarForm.patchValue({
  movimientoLugarDescripcion: lugar.movimientoLugarDescripcion,
  movimientoLugarActivo: lugar.movimientoLugarActivo,
  movimientoLugarPrioridad: lugar.movimientoLugarPrioridad
  });
    this.mostrarFormulario = true;
  }

  cancelar(): void {
    this.mostrarFormulario = false;
  }

  guardar(): void {
    if (this.lugarForm.invalid) return;

const formData: MovimientoLugar = {
  ...this.lugarForm.value,
  movimientoLugarId: this.lugarActual.movimientoLugarId
};

    this.lugarService.guardarLugar(formData).subscribe({
      next: () => {
        this.cargarLugares();
        this.mostrarFormulario = false;
      },
      error: (err) => console.error('Error al guardar', err)
    });
  }

  // =========================
  // TOGGLE STATE (NOW FULLY SYMMETRIC)
  // =========================
  toggleEstado(lugar: MovimientoLugar): void {
  if (!lugar.movimientoLugarId) return;

  if (lugar.movimientoLugarActivo) {
    // Si está activo, desactivamos (Soft-delete)
    this.lugarService.desactivarLugar(lugar.movimientoLugarId).subscribe({
      next: () => {
        console.log('Desactivado con éxito');
        this.cargarLugares(); // Forzamos la recarga de la lista
      },
      error: (err) => console.error('Error al desactivar', err)
    });
  } else {
    // Para reactivar, creamos el objeto asegurando el boolean true
    const lugarReactivado: MovimientoLugar = { 
      ...lugar, 
      movimientoLugarActivo: true 
    };

    this.lugarService.guardarLugar(lugarReactivado).subscribe({
      next: (res) => {
        console.log('Reactivado con éxito', res);
        this.cargarLugares();
      },
      error: (err) => console.error('Error al reactivar', err)
    });
  }
}

  borrar(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este lugar para siempre?')) {
      this.lugarService.eliminarLugar(id).subscribe(() => this.cargarLugares());
    }
  }
}