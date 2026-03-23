import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone'; 
import { provideHttpClient } from '@angular/common/http'; // 1. ADD THIS IMPORT

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideIonicAngular({
      mode: 'ios' // Forces the professional iOS look
    }),
    provideHttpClient() // 2. ADD THIS LINE HERE
  ],
});