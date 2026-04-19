import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoriaService, Categoria } from '../../services/categoria/categoria';
import { DescuentoService, Descuento } from '../../services/descuento/descuento';

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categorias.html',
  styleUrls: ['./categorias.css'] // Si usas SCSS, cambia la extensión
})
export class CategoriasComponent implements OnInit {

  categorias: Categoria[] = [];
  descuentos: Descuento[] = [];
  mostrarFormulario = false;
  categoriaActual: Categoria = { categoriaNombre: '' };
  mensajeError = '';

  constructor(
    private categoriaService: CategoriaService,
    private descuentoService: DescuentoService
  ) {}

  ngOnInit() {
    this.cargarCategorias();
    this.cargarDescuentos();
  }

  cargarCategorias() {
    this.categoriaService.obtenerCategorias().subscribe({
      next: (datos) => this.categorias = datos,
      error: (err) => console.error('Error al cargar categorías', err)
    });
  }

  cargarDescuentos() {
    this.descuentoService.obtenerDescuentosActivos().subscribe({
      next: (datos) => this.descuentos = datos,
      error: (err) => console.error('Error al cargar descuentos', err)
    });
  }

  abrirNuevo() {
    this.categoriaActual = { categoriaNombre: '' };
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  editarCategoria(cat: Categoria) {
    this.categoriaActual = { ...cat };
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  compararDescuentos(d1: any, d2: any): boolean {
    return d1 && d2 ? d1.descuentoId === d2.descuentoId : d1 === d2;
  }

  guardarCategoria() {
    if (this.categoriaActual.categoriaId) {
      this.categoriaService.actualizarCategoria(this.categoriaActual.categoriaId, this.categoriaActual)
        .subscribe({
          next: () => {
            this.cargarCategorias();
            this.mostrarFormulario = false;
          }
        });
    } else {
      this.categoriaService.crearCategoria(this.categoriaActual)
        .subscribe({
          next: () => {
            this.cargarCategorias();
            this.mostrarFormulario = false;
          }
        });
    }
  }

  eliminarCategoria(id: number | undefined) {
    if (!id) return;
    this.mensajeError = ''; // Limpiamos errores previos

    if (confirm('¿Seguro que deseas eliminar esta categoría?')) {
      this.categoriaService.eliminarCategoria(id).subscribe({
        next: () => this.cargarCategorias(),
        error: (err) => {
          // AQUÍ ATAJAMOS EL ERROR DEL RF005 (Si tiene productos asociados)
          if (err.status === 409 || err.status === 400 || err.status === 500) {
             this.mensajeError = '⚠️ No se puede eliminar: Esta categoría tiene productos asociados.';
             // En un futuro podemos cambiar este alert por un "Toast" o alerta más bonita en el HTML
             alert(this.mensajeError); 
          }
        }
      });
    }
  }

  cancelar() {
    this.mostrarFormulario = false;
  }
}