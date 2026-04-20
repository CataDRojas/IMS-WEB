import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CategoriaService, Categoria } from '../../services/categoria/categoria';

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categorias.html',
  styleUrls: ['./categorias.css']
})
export class CategoriasComponent implements OnInit {

  categorias: Categoria[] = [];

  mostrarFormulario = false;

  categoriaActual: Categoria = this.crearCategoriaVacia();

  mensajeError = '';
  mensajeExito = '';

  constructor(private categoriaService: CategoriaService) {}

  ngOnInit(): void {
    this.cargarCategorias();
  }

  // =========================
  // DATA
  // =========================
  cargarCategorias(): void {
    this.categoriaService.obtenerCategorias().subscribe({
      next: (data) => {
        this.categorias = data;
      },
      error: () => {
        this.mensajeError = 'Error al cargar categorías';
      }
    });
  }

  // =========================
  // FORM STATE
  // =========================
  crearCategoriaVacia(): Categoria {
    return {
      categoriaNombre: ''
    };
  }

  abrirNuevo(): void {
    this.categoriaActual = this.crearCategoriaVacia();
    this.mensajeError = '';
    this.mensajeExito = '';
    this.mostrarFormulario = true;
  }

  editarCategoria(cat: Categoria): void {
    this.categoriaActual = { ...cat };
    this.mensajeError = '';
    this.mensajeExito = '';
    this.mostrarFormulario = true;
  }

  cancelar(): void {
    this.mostrarFormulario = false;
  }

  // =========================
  // SAVE FLOW
  // =========================
  guardarCategoria(): void {

    const request$ = this.categoriaActual.categoriaId
      ? this.categoriaService.actualizarCategoria(
          this.categoriaActual.categoriaId,
          this.categoriaActual
        )
      : this.categoriaService.crearCategoria(this.categoriaActual);

    request$.subscribe({
      next: () => {
        this.mostrarFormulario = false;
        this.mensajeError = '';
        this.mensajeExito = 'Categoría guardada correctamente';
        this.cargarCategorias();
      },
      error: () => {
        this.mensajeError = 'Error al guardar categoría';
        this.mensajeExito = '';
      }
    });
  }

  // =========================
  // DELETE FLOW
  // =========================
  eliminarCategoria(id: number | undefined): void {

    if (!id) return;

    this.mensajeError = '';
    this.mensajeExito = '';

    if (!confirm('¿Seguro que deseas eliminar esta categoría?')) return;

    this.categoriaService.eliminarCategoria(id).subscribe({
      next: () => {
        this.mensajeExito = 'Categoría eliminada correctamente';
        this.cargarCategorias();
      },
      error: (err) => {
        if (err.status === 409 || err.status === 400) {
          this.mensajeError =
            'No se puede eliminar: existen productos asociados a esta categoría.';
        } else {
          this.mensajeError = 'Error al eliminar categoría';
        }
      }
    });
  }
}