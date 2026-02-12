import Swal from 'sweetalert2';

export const Toast = Swal.mixin({
  toast: true,
  position: 'top-end', // Góc trên bên phải
  showConfirmButton: false,
  timer: 3000, // Tự tắt sau 3 giây
  timerProgressBar: true,
  didOpen: (toast) => {
    toast.addEventListener('mouseenter', Swal.stopTimer)
    toast.addEventListener('mouseleave', Swal.resumeTimer)
  }
});

// Hàm gọi nhanh
export const toastSuccess = (title) => {
    Toast.fire({
        icon: 'success',
        title: title
    });
};

export const toastError = (title) => {
    Toast.fire({
        icon: 'error',
        title: title
    });
};