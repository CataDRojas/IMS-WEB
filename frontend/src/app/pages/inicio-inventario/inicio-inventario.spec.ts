import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InicioInventario } from './inicio-inventario';

describe('InicioInventario', () => {
  let component: InicioInventario;
  let fixture: ComponentFixture<InicioInventario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InicioInventario]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InicioInventario);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
