import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DescuentosForm } from './descuentos-form';

describe('DescuentosForm', () => {
  let component: DescuentosForm;
  let fixture: ComponentFixture<DescuentosForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DescuentosForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DescuentosForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
