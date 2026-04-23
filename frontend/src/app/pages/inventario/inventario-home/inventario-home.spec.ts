import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InventarioHome } from './inventario-home';

describe('InventarioHome', () => {
  let component: InventarioHome;
  let fixture: ComponentFixture<InventarioHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventarioHome]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InventarioHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
