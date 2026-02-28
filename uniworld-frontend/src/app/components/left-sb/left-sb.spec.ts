import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeftSb } from './left-sb';

describe('LeftSb', () => {
  let component: LeftSb;
  let fixture: ComponentFixture<LeftSb>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LeftSb]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LeftSb);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
