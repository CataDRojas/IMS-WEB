import { Component, OnInit } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../../services/usuario/usuario';

export interface Rol {
  rolId: number;
  rolNombre: string;
}

export interface Usuario {
  usuarioId?: number;
  usuarioEmail: string;
  usuarioNombre: string;
  usuarioRun: string;
  usuarioDV: string;
  usuarioPassword?: string;
  rol: Rol;
  usuarioActivo: boolean;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class UsuariosComponent implements OnInit { 
  
  // Lista de roles
  rolesDisponibles: Rol[] = [
    { rolId: 1, rolNombre: 'ADMIN' },
    { rolId: 2, rolNombre: 'VENDEDOR' },
    { rolId: 3, rolNombre: 'BODEGUERO' }
  ];

  listaUsuarios: Usuario[] = [];

  mostrarFormulario = false;
  usuarioActual: Usuario = this.crearUsuarioVacio();

  constructor(private usuarioService: UsuarioService) {}

  ngOnInit() {
    this.cargarUsuarios();
  }

  cargarUsuarios() {
    this.usuarioService.obtenerUsuarios().subscribe({
      next: (datosDelBackend) => {
        console.log('¡Usuarios recibidos desde Spring Boot!', datosDelBackend);
        this.listaUsuarios = datosDelBackend; 
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
      }
    });
  }

  crearUsuarioVacio(): Usuario {
    return {
      usuarioEmail: '', usuarioNombre: '', usuarioRun: '', usuarioDV: '',
      usuarioPassword: '', rol: this.rolesDisponibles[1], usuarioActivo: true
    };
  }

  abrirNuevo() {
    this.usuarioActual = this.crearUsuarioVacio();
    this.mostrarFormulario = true;
  }

  editarUsuario(usuario: Usuario) {
    this.usuarioActual = { ...usuario };
    this.usuarioActual.usuarioPassword = ''; 
    this.mostrarFormulario = true;
  }

  guardarUsuario() {
    if (this.usuarioActual.usuarioId) {
      this.usuarioService.actualizarUsuario(this.usuarioActual.usuarioId, this.usuarioActual).subscribe({
        next: () => {
          console.log('¡Usuario actualizado en la BD!');
          this.cargarUsuarios();
          this.mostrarFormulario = false;
        },
        error: (err) => console.error('Error al actualizar', err)
      });
    } else {
      this.usuarioService.crearUsuario(this.usuarioActual).subscribe({
        next: () => {
          console.log('¡Usuario creado en la BD!');
          this.cargarUsuarios();
          this.mostrarFormulario = false;
        },
        error: (err) => console.error('Error al crear', err)
      });
    }
  }

  eliminarUsuario(id: number | undefined) {
    if (!id) return; 
    
    const confirmar = confirm('¿Estás seguro de que deseas eliminar este usuario del sistema?');
    
    if (confirmar) {
      this.usuarioService.eliminarUsuario(id).subscribe({
        next: () => {
          console.log('¡Usuario eliminado de la BD!');
          this.cargarUsuarios(); 
        },
        error: (err) => console.error('Error al eliminar', err)
      });
    }
  }

  cancelar() {
    this.mostrarFormulario = false;
  }
}