import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class UiController {
  // 2026 Standard: Signals for instant UI updates
  showLayout = signal(false); 

  setHasLayout(value: boolean) {
    this.showLayout.set(value);
  }
}