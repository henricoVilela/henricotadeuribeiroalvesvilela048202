import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading',
  standalone: true,
  template: `
    <div class="flex items-center justify-center" [class.py-12]="fullPage">
      <svg 
        class="animate-spin text-primary-600" 
        [class.h-5]="size === 'sm'"
        [class.w-5]="size === 'sm'"
        [class.h-8]="size === 'md'"
        [class.w-8]="size === 'md'"
        [class.h-12]="size === 'lg'"
        [class.w-12]="size === 'lg'"
        xmlns="http://www.w3.org/2000/svg" 
        fill="none" 
        viewBox="0 0 24 24"
      >
        <circle 
          class="opacity-25" 
          cx="12" 
          cy="12" 
          r="10" 
          stroke="currentColor" 
          stroke-width="4"
        ></circle>
        <path 
          class="opacity-75" 
          fill="currentColor" 
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        ></path>
      </svg>
      @if (text) {
        <span class="ml-3 text-gray-600">{{ text }}</span>
      }
    </div>
  `
})
export class Loading {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() text?: string;
  @Input() fullPage = false;
}
