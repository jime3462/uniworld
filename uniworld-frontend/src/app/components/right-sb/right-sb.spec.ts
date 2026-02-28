import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RightSb } from './right-sb';

describe('RightSb', () => {
  let component: RightSb;
  let fixture: ComponentFixture<RightSb>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RightSb]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RightSb);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
