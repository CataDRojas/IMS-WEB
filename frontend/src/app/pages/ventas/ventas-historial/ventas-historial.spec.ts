import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VentasHistorial } from './ventas-historial';

describe('VentasHistorial', () => {
  let component: VentasHistorial;
  let fixture: ComponentFixture<VentasHistorial>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VentasHistorial]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VentasHistorial);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
