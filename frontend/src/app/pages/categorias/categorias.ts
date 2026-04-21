import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { CategoriaService, Categoria } from '../../services/categoria/categoria';
import { DescuentosService, Descuento } from '../../services/descuento/descuento'; // 🔥 1. RESTAURADO EL IMPORT

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categorias.html',
  styleUrls: ['./categorias.css']
})
export class CategoriasComponent implements OnInit {

  categorias: Categoria[] = [];
  descuentos: Descuento[] = []; // 🔥 2. LISTA PARA GUARDAR LOS DESCUENTOS DEL BACKEND

  mostrarFormulario = false;

  // 🔥 3. Usamos 'any' para evitar que TypeScript llore si Categoria no tiene 'descuento' definido en la interfaz
  categoriaActual: any = this.crearCategoriaVacia();

  mensajeError = '';
  mensajeExito = '';

  constructor(
    private categoriaService: CategoriaService, 
    private descuentoService: DescuentosService, // 🔥 4. INYECTAMOS EL SERVICIO
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarDescuentos(); // 🔥 5. CARGAMOS LOS DESCUENTOS AL INICIAR
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

  // 🔥 6. FUNCIÓN QUE TRAE LOS DESCUENTOS
  cargarDescuentos(): void {
    this.descuentoService.getActive().subscribe({
      next: (data: Descuento[]) => this.descuentos = data,
      error: (err: any) => console.error('Error al cargar descuentos', err)
    });
  }

  // =========================
  // FORM STATE
  // =========================
  crearCategoriaVacia(): any {
    return {
      categoriaNombre: '',
      descuento: null // 🔥 7. EL DESCUENTO EMPIEZA NULO
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

  // 🔥 8. EL COMPARADOR PARA QUE EL <SELECT> DEL HTML FUNCIONE PERFECTO
  compararDescuentos(d1: any, d2: any): boolean {
    return d1 && d2 ? d1.descuentoId === d2.descuentoId : d1 === d2;
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
        if (err.status === 409 || err.status === 400 || err.status === 500) {
          this.mensajeError =
            'No se puede eliminar: existen productos asociados a esta categoría.';
        } else {
          this.mensajeError = 'Error al eliminar categoría';
        }
      }
    });
  }

  goHome() {
    this.router.navigate(['/home']);
  }
}