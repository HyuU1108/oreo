// 카테고리 토글 및 관련 로직
document.addEventListener('DOMContentLoaded', function () {
  const categorySelect = document.getElementById('categorySelect');
  const newCategoryInput = document.getElementById('newCategoryInput');
  const finalCategory = document.getElementById('finalCategory'); // hidden input
  const toggleCategoryBtn = document.getElementById('toggleCategoryBtn');
  const categorySelectWrapper = document.getElementById(
    'categorySelectWrapper'
  );
  const newCategoryWrapper = document.getElementById('newCategoryWrapper');
  const categoryFeedback = document.getElementById('categoryFeedback');
  const form = document.querySelector('.needs-validation'); // form 참조

  // Thymeleaf에서 전달된 변수 사용 (HTML의 인라인 스크립트에서 선언됨)
  // const existingCategories = allCategories; // HTML에서 선언됨
  // const initialCategoryValue = initialCategory; // HTML에서 선언됨
  // const isEditMode = isEditMode; // HTML에서 선언됨
  // const hasCategoryError = hasCategoryError; // HTML에서 선언됨

  let isAddingNew = false;

  function clearCategoryValidation() {
    if (categorySelect) categorySelect.classList.remove('is-invalid');
    if (newCategoryInput) newCategoryInput.classList.remove('is-invalid');
    if (categoryFeedback) categoryFeedback.textContent = '';
    // 폼의 전반적인 유효성 상태도 업데이트 (선택 사항)
    // form.classList.remove('was-validated');
  }

  function switchToCategorySelect() {
    clearCategoryValidation();
    if (categorySelectWrapper) categorySelectWrapper.classList.remove('d-none');
    if (newCategoryWrapper) newCategoryWrapper.classList.add('d-none');
    if (toggleCategoryBtn) {
      toggleCategoryBtn.innerHTML = '<i class="bi bi-pencil-fill"></i>';
      toggleCategoryBtn.title = '새 카테고리 직접 입력';
    }
    if (categorySelect) {
      // HTML의 전역 변수 사용
      if (
        !(
          typeof isEditMode !== 'undefined' &&
          isEditMode &&
          typeof initialCategoryValue !== 'undefined' &&
          initialCategoryValue &&
          typeof allCategories !== 'undefined' &&
          !allCategories.includes(initialCategoryValue)
        )
      ) {
        categorySelect.value = initialCategoryValue || '';
      } else {
        categorySelect.value = '';
      }
      if (finalCategory) finalCategory.value = categorySelect.value;
    }
    if (newCategoryInput) newCategoryInput.value = '';
    isAddingNew = false;
  }

  function switchToNewCategoryInput() {
    clearCategoryValidation();
    if (categorySelectWrapper) categorySelectWrapper.classList.add('d-none');
    if (newCategoryWrapper) newCategoryWrapper.classList.remove('d-none');
    if (toggleCategoryBtn) {
      toggleCategoryBtn.innerHTML = '<i class="bi bi-list-ul"></i>';
      toggleCategoryBtn.title = '기존 카테고리에서 선택';
    }
    // HTML의 전역 변수 사용
    if (
      typeof isEditMode !== 'undefined' &&
      isEditMode &&
      typeof initialCategoryValue !== 'undefined' &&
      initialCategoryValue &&
      typeof allCategories !== 'undefined' &&
      !allCategories.includes(initialCategoryValue)
    ) {
      if (newCategoryInput) newCategoryInput.value = initialCategoryValue;
      if (finalCategory) finalCategory.value = initialCategoryValue;
    } else {
      if (newCategoryInput) newCategoryInput.value = '';
      if (finalCategory) finalCategory.value = '';
    }
    if (newCategoryInput) newCategoryInput.focus();
    isAddingNew = true;
  }

  // 초기 상태 설정 (HTML에서 전달된 변수 사용 확인)
  if (
    typeof isEditMode !== 'undefined' &&
    isEditMode &&
    typeof initialCategoryValue !== 'undefined' &&
    initialCategoryValue &&
    typeof allCategories !== 'undefined' &&
    !allCategories.includes(initialCategoryValue)
  ) {
    switchToNewCategoryInput();
  } else {
    switchToCategorySelect();
  }

  // 카테고리 유효성 검사 실패 시 해당 입력 필드 표시
  if (typeof hasCategoryError !== 'undefined' && hasCategoryError) {
    if (isAddingNew) {
      // 에러 발생 시 입력 모드였다면
      switchToNewCategoryInput();
      if (newCategoryInput) newCategoryInput.classList.add('is-invalid');
    } else {
      // 에러 발생 시 선택 모드였다면
      switchToCategorySelect();
      if (categorySelect) categorySelect.classList.add('is-invalid');
    }
  }

  // 토글 버튼 이벤트
  if (toggleCategoryBtn) {
    toggleCategoryBtn.addEventListener('click', function () {
      if (isAddingNew) {
        switchToCategorySelect();
      } else {
        switchToNewCategoryInput();
      }
    });
  }

  // 선택 변경 이벤트
  if (categorySelect) {
    categorySelect.addEventListener('change', function () {
      if (!isAddingNew) {
        if (finalCategory) finalCategory.value = this.value;
        clearCategoryValidation();
      }
    });
  }

  // 직접 입력 이벤트
  if (newCategoryInput) {
    newCategoryInput.addEventListener('input', function () {
      if (isAddingNew) {
        if (finalCategory) finalCategory.value = this.value.trim();
        clearCategoryValidation();
      }
    });
  }

  // 폼 제출 시 카테고리 최종 값 확인 및 유효성 검사 로직 (validation.js와 연동 또는 별도 처리)
  if (form) {
    form.addEventListener('submit', (event) => {
      if (finalCategory && !finalCategory.value) {
        // 최종 카테고리 값이 비어있으면
        clearCategoryValidation(); // 일단 기존 오류 지우고
        if (categorySelect && !categorySelect.closest('.d-none')) {
          categorySelect.classList.add('is-invalid');
        } else if (newCategoryInput && !newCategoryInput.closest('.d-none')) {
          newCategoryInput.classList.add('is-invalid');
        }
        if (categoryFeedback)
          categoryFeedback.textContent = '카테고리를 선택하거나 입력해주세요.';
        // Bootstrap 기본 유효성 검사와 별개로 제출 막기
        event.preventDefault();
        event.stopPropagation();
        form.classList.add('was-validated'); // 유효성 검사 스타일 적용
      }
    });
  }

  // form.html 내 미리보기 이미지 에러 처리 (image-error.js로 통합 가능)
  // document.querySelectorAll('.current-image-preview img').forEach(img => {
  //      img.addEventListener('error', () => {
  //          const errorDiv = img.nextElementSibling;
  //          if (errorDiv && errorDiv.classList.contains('img-load-error')) {
  //              img.style.display = 'none';
  //              errorDiv.style.display = 'block';
  //          }
  //      });
  // });
});
