import { Component } from '@angular/core';
import { Layout } from '../../../shared/components/layout/layout.component';
import { Loading } from '../../../shared/components/loaging/loading.component';

@Component({
  selector: 'app-album-list',
  imports: [
    Layout,
    Loading,
  ],
  templateUrl: './album-list.html',
})
export class AlbumList {

}
