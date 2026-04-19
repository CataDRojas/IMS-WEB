import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InventarioForm } from './inventario-form';

describe('InventarioForm', () => {
  let component: InventarioForm;
  let fixture: ComponentFixture<InventarioForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventarioForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InventarioForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
