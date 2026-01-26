import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let message = 'Ocorreu um erro inesperado';

      if (error.error?.message) {
        message = error.error.message;
      } else {
        switch (error.status) {
          case 0:
            message = 'Não foi possível conectar ao servidor';
            break;
          case 400:
            message = 'Requisição inválida';
            break;
          case 401:
            message = 'Sessão expirada. Faça login novamente';
            break;
          case 403:
            message = 'Você não tem permissão para esta ação';
            break;
          case 404:
            message = 'Recurso não encontrado';
            break;
          case 409:
            message = 'Conflito: operação já em andamento';
            break;
          case 429:
            message = 'Muitas requisições. Aguarde um momento';
            break;
          case 500:
            message = 'Erro interno do servidor';
            break;
        }
      }

      if (error.status === 400 && error.error?.errors) {
        const validationErrors = error.error.errors;
        const errorMessages = Object.values(validationErrors).flat();
        message = errorMessages.join(' ');
      }
      
      toastService.error(message);
    
      return throwError(() => error);
    })
  );
};
