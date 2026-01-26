import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
    },
    {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login').then(m => m.Login),
        canActivate: [guestGuard]
    },
    {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then(m => m.Dashboard),
        canActivate: [authGuard]
    },
    {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register').then(m => m.Register),
        canActivate: [guestGuard]
    },
    {
        path: 'albuns',
        canActivate: [authGuard],
        children: [
            {
                path: '',
                loadComponent: () => import('./features/albuns/album-list/album-list').then(m => m.AlbumList)
            },
            {
                path: 'novo',
                loadComponent: () => import('./features/albuns/album-form/album-form').then(m => m.AlbumForm)
            },
            {
                path: ':id',
                loadComponent: () => import('./features/albuns/album-detail/album-detail').then(m => m.AlbumDetail)
            },
            {
                path: ':id/editar',
                loadComponent: () => import('./features/albuns/album-form/album-form').then(m => m.AlbumForm)
            }
        ]
    },
    {
        path: 'artistas',
        canActivate: [authGuard],
        children: [
            {
                path: '',
                loadComponent: () => import('./features/artistas/artista-list/artista-list').then(m => m.ArtistaList)
            },
            {
                path: ':id',
                loadComponent: () => import('./features/artistas/artista-detail/artista-detail').then(m => m.ArtistaDetail)
            }
        ]
    },
    {
        path: '**',
        redirectTo: 'dashboard'
    }
];
