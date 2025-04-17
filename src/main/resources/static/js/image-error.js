// 이미지 로드 에러 처리 (플레이스홀더 표시용)
document.addEventListener(
  'error',
  function (event) {
    // list.html, favorite/list.html 의 카드 이미지 에러 처리
    if (
      event.target.tagName === 'IMG' &&
      event.target.classList.contains('list-card-img')
    ) {
      const placeholder = event.target.parentElement.querySelector(
        '.img-placeholder.error'
      );
      if (placeholder) {
        event.target.style.display = 'none';
        placeholder.classList.remove('d-none');
        placeholder.style.display = 'flex';
      }
    }
    // detail.html 의 미리보기 이미지 에러 처리
    else if (
      event.target.tagName === 'IMG' &&
      event.target.closest('.image-display-area')
    ) {
      const errorDiv = event.target.nextElementSibling;
      if (errorDiv && errorDiv.classList.contains('img-load-error')) {
        event.target.style.display = 'none';
        errorDiv.style.display = 'block';
      }
    }
    // form.html 의 현재 이미지 미리보기 에러 처리
    else if (
      event.target.tagName === 'IMG' &&
      event.target.closest('.current-image-preview')
    ) {
      const errorDiv = event.target.nextElementSibling;
      if (errorDiv && errorDiv.classList.contains('img-load-error')) {
        event.target.style.display = 'none';
        errorDiv.style.display = 'block';
      }
    }
  },
  true
); // Capture phase
