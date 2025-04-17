// Bootstrap 5 Form Validation Script
(() => {
  'use strict';
  const forms = document.querySelectorAll('.needs-validation');
  Array.from(forms).forEach((form) => {
    form.addEventListener(
      'submit',
      (event) => {
        // 추가적인 커스텀 유효성 검사 로직이 필요하다면 여기에 추가 가능
        // 예: form.id === 'review-form' 인 경우 특정 필드 검사

        if (!form.checkValidity()) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      },
      false
    );
  });
})();
