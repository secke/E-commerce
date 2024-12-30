import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { TokenService } from './services/token.service';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'buy-01';
  // tokenService = inject(TokenService);
  constructor(private router: Router, public tokenService: TokenService){}
  handleUserLogoClick(): void {
    if (!this.tokenService.getToken()) {
      this.router.navigate(['/auth/login']);
    } else {
      console.log('User is already logged in');
      this.router.navigate(['myProducts']);
    }
  }
  
}
