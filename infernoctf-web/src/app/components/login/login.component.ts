// login.component.ts
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { ApiResponse } from '../../models/api-response.model';
import { LoginResponseDTO } from '../../models/dto/login-response.dto.model';
import { User } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';
import { LoginService } from '../../services/login.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup = new FormGroup({});
  wrongPassword = false;
  busy = false;
  hidePassword = true;
  rememberMe = false;
  errorMessage = '';
  loginAttempts = 0;
  maxLoginAttempts = 5;
  isLocked = false;
  lockoutTimeLeft = 0;
  lockoutTimer: any;
  
  @Output() forgotPasswordClicked = new EventEmitter<void>();
  @Output() registerClicked = new EventEmitter<void>();

  Math = Math;

  constructor(
    private loginService: LoginService,
    private authService: AuthService,
    private router: Router,
    private dialogRef: MatDialogRef<LoginComponent>,
    private userService: UserService,
    private fb: FormBuilder
  ) { }

  ngOnInit(): void {
    // Initialize the form with validators
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
    
    // Check for remembered user
    const rememberedUser = localStorage.getItem('rememberedUser');
    if (rememberedUser) {
      this.loginForm.patchValue({ username: rememberedUser });
      this.rememberMe = true;
    }
  }

  ngOnDestroy(): void {
    if (this.lockoutTimer) {
      clearInterval(this.lockoutTimer);
    }
  }

  public loginClick(): void {
    // Mark all fields as touched to trigger validation
    this.loginForm!.markAllAsTouched();
    
    // Return if form is invalid
    if (this.loginForm!.invalid) {
      return;
    }
    
    // Check if account is locked
    if (this.isLocked) {
      this.errorMessage = `Account temporarily locked. Please try again in ${Math.ceil(this.lockoutTimeLeft / 1000)} seconds.`;
      return;
    }
    
    this.busy = true;
    this.errorMessage = '';
    
    const username = this.loginForm!.get('username')!.value;
    const password = this.loginForm!.get('password')!.value;
    
    this.loginService.login(username, password)
      .pipe(finalize(() => this.busy = false))
      .subscribe({
        next: (response: ApiResponse<LoginResponseDTO>) => {
          if (!response.data?.jwt) {
            this.handleLoginError('No valid JWT token received');
            return;
          }
          
          try {
            const tokenPayload = JSON.parse(atob(response.data.jwt.split('.')[1]));
            
            // Reset error state
            this.wrongPassword = false;
            this.loginAttempts = 0;
            
            // Handle remember me functionality
            if (this.rememberMe) {
              localStorage.setItem('rememberedUser', username);
            } else {
              localStorage.removeItem('rememberedUser');
            }
            
            // Store token
            localStorage.setItem('jwt', response.data.jwt);
            
            if (!response.data.user) {
              this.handleLoginError('No user data received');
              return;
            }
            
            // Set user data and navigate
            const user = new User(response.data.user);
            console.log('logging in', response.data.user)
            this.authService.setPayload(user, tokenPayload);
            this.router.navigate(['/home']);
            this.dialogRef.close(true);
          } catch (e) {
            console.error('Failed to process JWT token', e);
            this.handleLoginError('Failed to process authentication token');
          }
        },
        error: (error) => {
          this.handleLoginError(error);
        }
      });
  }
  
  private handleLoginError(error: any): void {
    this.loginAttempts++;
    
    if (error.status === 401 || error === 'No valid JWT token received' || error === 'No user data received') {
      this.wrongPassword = true;
      this.errorMessage = 'Invalid username or password';
    } else {
      this.errorMessage = `Login failed: ${error.message || error}`;
    }
    
    // Implement account lockout after too many failed attempts
    if (this.loginAttempts >= this.maxLoginAttempts) {
      this.isLocked = true;
      this.lockoutTimeLeft = 60000; // 1 minute lockout
      
      this.errorMessage = `Too many failed attempts. Account temporarily locked for 60 seconds.`;
      
      this.lockoutTimer = setInterval(() => {
        this.lockoutTimeLeft -= 1000;
        if (this.lockoutTimeLeft <= 0) {
          this.isLocked = false;
          this.loginAttempts = 0;
          clearInterval(this.lockoutTimer);
        }
      }, 1000);
    }
    
    console.warn(this.errorMessage);
  }
  
  public forgotPassword(): void {
    this.forgotPasswordClicked.emit();
  }
  
  public register(): void {
    this.registerClicked.emit();
  }
  
  // Helper getters for template access
  get usernameControl(): FormControl { return this.loginForm.get('username') as FormControl; }
  get passwordControl(): FormControl { return this.loginForm.get('password') as FormControl; }
}