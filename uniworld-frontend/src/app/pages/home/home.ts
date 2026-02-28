import { Component } from '@angular/core';
import { LeftSb } from '../../components/left-sb/left-sb';
import { RightSb } from '../../components/right-sb/right-sb';

@Component({
  selector: 'app-home',
  imports: [LeftSb, RightSb],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {

}
