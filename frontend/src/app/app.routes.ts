import { Routes } from '@angular/router';
import { DocumentList } from './document-list/document-list';
import { DocumentEditor } from './document-editor/document-editor';

export const routes: Routes = [
  {path: 'documents', component: DocumentList},
  {path: 'documents/:id', component: DocumentEditor},
  {path: '', redirectTo: 'documents', pathMatch: 'full'}
  ];
