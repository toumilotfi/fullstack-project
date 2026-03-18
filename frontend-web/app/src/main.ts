import { bootstrapApplication } from '@angular/platform-browser';
import { RouteReuseStrategy, provideRouter, withPreloading, PreloadAllModules } from '@angular/router';
import { IonicRouteStrategy, provideIonicAngular } from '@ionic/angular/standalone';
import { App } from './app/app'; // <--- Notice it imports 'App' from './app/app'
import { routes } from './app/app.routes';
import { addIcons } from 'ionicons';
import * as icons from 'ionicons/icons';

addIcons(icons);
bootstrapApplication(App, {
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    provideIonicAngular({ mode: 'md' }), // Force Desktop Look
    provideRouter(routes, withPreloading(PreloadAllModules)),
  ],
});