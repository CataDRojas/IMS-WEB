import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, map } from 'rxjs';
import { UsuarioService } from '../../services/usuario/usuario';
import { Router } from '@angular/router';

//angular materials
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';

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
  rolId: number;
  usuarioActivo: boolean;
}

interface UsuarioRaw {
  usuarioId?: number;
  usuarioEmail: string;
  usuarioNombre: string;
  usuarioRun: string;
  usuarioDV: string;
  usuarioPassword?: string;
  usuarioActivo: boolean;

  rolId?: number;
  rol?: Rol;
}

interface UsuarioView extends Usuario {
  rolNombre: string;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule, MatExpansionModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class UsuariosComponent implements OnInit {

  rolesDisponibles: Rol[] = [];
  listaUsuarios: Usuario[] = [];
  listaUsuariosView: UsuarioView[] = [];

  mostrarFormulario = false;

  usuarioActual: Usuario = this.crearUsuarioVacio();

  private rolesMap: Record<number, string> = {};

  constructor(private usuarioService: UsuarioService, private router: Router) {}

  ngOnInit() {
    this.cargarTodo();
  }

  cargarTodo() {
    forkJoin({
      roles: this.usuarioService.obtenerRoles(),
      usuarios: this.usuarioService.obtenerUsuarios()
    }).subscribe({
      next: ({ roles, usuarios }) => {

        this.rolesDisponibles = roles;

        this.rolesMap = roles.reduce((acc, r) => {
          acc[Number(r.rolId)] = r.rolNombre;
          return acc;
        }, {} as Record<number, string>);

        this.listaUsuarios = usuarios.map((u: any) => ({
          usuarioId: u.usuarioId,
          usuarioEmail: u.usuarioEmail,
          usuarioNombre: u.usuarioNombre,
          usuarioRun: u.usuarioRun,
          usuarioDV: u.usuarioDV,
          usuarioPassword: u.usuarioPassword,
          usuarioActivo: u.usuarioActivo,

          rolId: Number(u.rolId ?? u.rol?.rolId ?? 0)
        }));

        this.rebuildView();
      },
      error: (err) => console.error('Error cargando datos:', err)
    });
  }

  private rebuildView() {
    this.listaUsuariosView = this.listaUsuarios.map(u => ({
      ...u,
      rolNombre: this.rolesMap[Number(u.rolId)] ?? 'VACIO'
    }));
  }

  crearUsuarioVacio(): Usuario {
    return {
      usuarioEmail: '',
      usuarioNombre: '',
      usuarioRun: '',
      usuarioDV: '',
      usuarioPassword: '',
      rolId: 0,
      usuarioActivo: true
    };
  }

  abrirNuevo() {
    this.usuarioActual = this.crearUsuarioVacio();
    this.mostrarFormulario = true;
  }

  editarUsuario(usuario: Usuario) {
    this.usuarioActual = {
      ...usuario,
      rolId: Number(usuario.rolId ?? 0),
      usuarioPassword: ''
    };

    this.mostrarFormulario = true;
  }

  cancelar() {
    this.mostrarFormulario = false;
  }

  guardarUsuario() {

  const payload: any = {
    usuarioId: this.usuarioActual.usuarioId,
    usuarioEmail: this.usuarioActual.usuarioEmail,
    usuarioNombre: this.usuarioActual.usuarioNombre,
    usuarioRun: this.usuarioActual.usuarioRun,
    usuarioDV: this.usuarioActual.usuarioDV,
    usuarioPassword: this.usuarioActual.usuarioPassword,
    usuarioActivo: this.usuarioActual.usuarioActivo,

    rol: {
      rolId: Number(this.usuarioActual.rolId)
    }
  };

  if (payload.usuarioId) {
    this.usuarioService.actualizarUsuario(payload.usuarioId, payload).subscribe({
      next: () => this.cargarTodo(),
      error: (err) => console.error('Error al actualizar', err)
    });
  } else {
    this.usuarioService.crearUsuario(payload).subscribe({
      next: () => this.cargarTodo(),
      error: (err) => console.error('Error al crear', err)
    });
  }

  this.mostrarFormulario = false;
}

  eliminarUsuario(id: number | undefined) {
    if (!id) return;

    if (confirm('¿Eliminar usuario?')) {
      this.usuarioService.eliminarUsuario(id).subscribe({
        next: () => this.cargarTodo(),
        error: (err) => console.error('Error al eliminar', err)
      });
    }
  }

  goHome() {
    this.router.navigate(['/home']);
  }
}