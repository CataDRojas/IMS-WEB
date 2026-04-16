import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BusquedaInventario } from './busqueda-inventario';

describe('BusquedaInventario', () => {
  let component: BusquedaInventario;
  let fixture: ComponentFixture<BusquedaInventario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BusquedaInventario]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BusquedaInventario);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
