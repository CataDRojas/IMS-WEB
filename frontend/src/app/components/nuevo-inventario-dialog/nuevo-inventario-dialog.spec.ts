import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NuevoInventarioDialog } from './nuevo-inventario-dialog';

describe('NuevoInventarioDialog', () => {
  let component: NuevoInventarioDialog;
  let fixture: ComponentFixture<NuevoInventarioDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NuevoInventarioDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NuevoInventarioDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
