import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InventarioHistorial } from './inventario-historial';

describe('InventarioHistorial', () => {
  let component: InventarioHistorial;
  let fixture: ComponentFixture<InventarioHistorial>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventarioHistorial]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InventarioHistorial);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
