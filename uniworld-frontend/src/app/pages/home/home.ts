import { Component } from '@angular/core';
import { LeftSb } from '../../components/left-sb/left-sb';
import { RightSb } from '../../components/right-sb/right-sb';
import { Search } from '../../components/search/search';

@Component({
  selector: 'app-home',
  imports: [LeftSb, RightSb, Search],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {

}
