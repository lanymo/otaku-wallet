// ========== 전역 변수 ==========
let listTruthEyeActive = false;

// ========== 대시보드 - 통계 로드 ==========
async function loadStatistics() {
    try {
        const response = await fetch('/api/expenses/statistics');
        const stats = await response.json();

        // 실제 총액 (블러 처리)
        const totalAmountEl = document.getElementById('totalAmount');
        const realAmount = formatNumber(stats.totalAmount) + '원';
        totalAmountEl.setAttribute('data-real', realAmount);
        totalAmountEl.textContent = realAmount;  // 실제 금액 표시 (블러로 가려짐)

        // 표시 총액
        document.getElementById('displayAmount').textContent =
            formatNumber(stats.displayAmount) + '원';

        // 만족 지출
        document.getElementById('satisfiedCount').textContent =
            stats.satisfiedCount + '개';
    } catch (error) {
        console.error('통계 로드 실패:', error);
    }
}

// ========== 통계 - 진실의 눈 토글 ==========
function toggleTruthEye() {
    const totalAmountEl = document.getElementById('totalAmount');
    const btn = document.getElementById('truthEyeBtn');
    const isBlurred = totalAmountEl.classList.contains('blurred');

    if (isBlurred) {
        // 블러 제거 (보이기)
        totalAmountEl.classList.remove('blurred');
        btn.classList.add('active');
        btn.textContent = '🙈 가리기';
    } else {
        // 블러 추가 (가리기)
        totalAmountEl.classList.add('blurred');
        btn.classList.remove('active');
        btn.textContent = '👁️ 진실의 눈';
    }
}

// ========== 대시보드 - 지출 목록 로드 ==========
async function loadExpenses() {
    try {
        const response = await fetch('/api/expenses');
        const expenses = await response.json();

        const listContainer = document.getElementById('expenseList');

        if (expenses.length === 0) {
            listContainer.innerHTML = '<div class="empty">등록된 지출이 없습니다</div>';
            return;
        }

        listContainer.innerHTML = expenses.map(expense => {
            // 금액 표시 로직
            let amountDisplay;
            let amountClass = '';

            if (expense.satisfactionRating === 5) {
                // 5점: 0원 표시 (진실의 눈으로 실제 금액 확인 가능)
                amountDisplay = '0원 ✨';
                amountClass = 'zero satisfied-amount';
            } else {
                // 5점 아님: 실제 금액 그대로 표시
                amountDisplay = formatNumber(expense.displayAmount) + '원';
                amountClass = '';
            }

            return `
                <div class="expense-card ${expense.isSatisfied ? 'satisfied' : ''}">
                    <div class="expense-info">
                        <div class="expense-header">
                            <span class="expense-category">${expense.categoryEmoji}</span>
                            <span class="expense-category-name">${expense.category}</span>
                        </div>
                        <div class="expense-title">${expense.title}</div>
                        <div class="expense-amount ${amountClass}"
                             data-real="${formatNumber(expense.amount)}원"
                             data-display="${amountDisplay}"
                             data-rating="${expense.satisfactionRating}">
                            ${amountDisplay}
                        </div>
                        <div class="expense-stars">${'★'.repeat(expense.satisfactionRating)}${'☆'.repeat(5 - expense.satisfactionRating)}</div>
                        <div class="expense-description">${expense.description || ''}</div>
                        <div class="expense-date">${expense.purchaseDate}</div>
                    </div>
                    <div class="expense-actions">
                        <button class="btn btn-secondary" onclick="editExpense(${expense.id})">수정</button>
                        <button class="btn btn-danger" onclick="deleteExpense(${expense.id})">삭제</button>
                    </div>
                </div>
            `;
        }).join('');

        // 목록 로드 후 진실의 눈 상태 초기화
        listTruthEyeActive = false;

    } catch (error) {
        console.error('지출 목록 로드 실패:', error);
        document.getElementById('expenseList').innerHTML =
            '<div class="empty">데이터를 불러올 수 없습니다</div>';
    }
}

// ========== 지출 목록 - 진실의 눈 토글 ==========
function toggleListTruthEye() {
    listTruthEyeActive = !listTruthEyeActive;
    const btn = document.getElementById('listTruthEyeBtn');
    const amounts = document.querySelectorAll('.expense-amount');

    amounts.forEach(el => {
        const rating = parseInt(el.getAttribute('data-rating'));

        if (rating === 5) {
            // 5점 지출만 토글
            if (listTruthEyeActive) {
                // 진실의 눈 활성화: 실제 금액 보여주기
                el.textContent = el.getAttribute('data-real');
            } else {
                // 진실의 눈 비활성화: 0원으로
                el.textContent = el.getAttribute('data-display');
            }
        }
        // 5점 아닌 지출은 그대로 (아무 변화 없음)
    });

    // 버튼 상태 변경
    if (listTruthEyeActive) {
        btn.classList.add('active');
        btn.textContent = '🙈 가리기';
    } else {
        btn.classList.remove('active');
        btn.textContent = '👁️ 진실의 눈';
    }
}

// ========== 지출 삭제 ==========
async function deleteExpense(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`/api/expenses/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            alert('삭제되었습니다');
            loadStatistics();
            loadExpenses();
        } else {
            alert('삭제 실패');
        }
    } catch (error) {
        console.error('삭제 오류:', error);
        alert('오류가 발생했습니다');
    }
}

// ========== 지출 수정 (폼으로 이동) ==========
function editExpense(id) {
    window.location.href = `/form?id=${id}`;
}

// ========== 폼 초기화 ==========
async function initForm() {
    // URL 파라미터에서 ID 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const expenseId = urlParams.get('id');

    // 카테고리 선택
    initCategorySelection();

    // 별점 기능
    initStarRating();

    // 수정 모드인지 확인
    if (expenseId) {
        // 수정 모드
        document.querySelector('.form-title').textContent = '✏️ 지출 수정';
        document.querySelector('button[type="submit"]').textContent = '수정하기';
        await loadExpenseForEdit(expenseId);
    } else {
        // 등록 모드
        document.getElementById('purchaseDate').valueAsDate = new Date();
    }

    // 폼 제출
    initFormSubmit(expenseId);
}

// ========== 수정할 지출 데이터 로드 ==========
async function loadExpenseForEdit(id) {
    try {
        const response = await fetch(`/api/expenses/${id}`);
        if (!response.ok) {
            alert('지출 데이터를 불러올 수 없습니다');
            window.location.href = '/';
            return;
        }

        const expense = await response.json();

        // 폼에 데이터 채우기
        document.getElementById('title').value = expense.title;
        document.getElementById('amount').value = expense.amount;
        document.getElementById('category').value = expense.category;
        document.getElementById('satisfactionRating').value = expense.satisfactionRating;
        document.getElementById('purchaseDate').value = expense.purchaseDate;
        document.getElementById('description').value = expense.description || '';

        // 카테고리 버튼 선택 상태 표시
        const categoryBtn = document.querySelector(`button[data-category="${expense.category}"]`);
        if (categoryBtn) {
            categoryBtn.classList.add('selected');
        }

        // 별점 표시
        const stars = document.querySelectorAll('.star');
        stars.forEach((star, index) => {
            if (index < expense.satisfactionRating) {
                star.classList.add('filled');
                star.classList.remove('empty');
                star.textContent = '★';
            }
        });

        // 별점 텍스트 업데이트
        const ratingText = document.getElementById('ratingText');
        const messages = [
            '별을 드래그하거나 클릭하세요',
            '⭐ 별로예요 (1점)',
            '⭐⭐ 그저 그래요 (2점)',
            '⭐⭐⭐ 괜찮아요 (3점)',
            '⭐⭐⭐⭐ 좋아요! (4점)',
            '⭐⭐⭐⭐⭐ 최고예요! 0원 처리됩니다! (5점)'
        ];
        ratingText.textContent = messages[expense.satisfactionRating];
        if (expense.satisfactionRating === 5) {
            ratingText.classList.add('perfect');
        }

    } catch (error) {
        console.error('데이터 로드 오류:', error);
        alert('오류가 발생했습니다');
        window.location.href = '/';
    }
}

// ========== 카테고리 선택 ==========
function initCategorySelection() {
    const categoryBtns = document.querySelectorAll('.category-btn');
    const categoryInput = document.getElementById('category');

    categoryBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            categoryBtns.forEach(b => b.classList.remove('selected'));
            this.classList.add('selected');
            categoryInput.value = this.getAttribute('data-category');
        });
    });
}

// ========== 별점 기능 ==========
function initStarRating() {
    const stars = document.querySelectorAll('.star');
    const ratingInput = document.getElementById('satisfactionRating');
    const ratingText = document.getElementById('ratingText');

    let currentRating = 0;
    let isMouseDown = false;

    function updateStars(rating, isTemp = false) {
        stars.forEach((star, index) => {
            if (index < rating) {
                star.classList.add('filled');
                star.classList.remove('empty');
                star.textContent = '★';
            } else {
                star.classList.remove('filled');
                star.classList.add('empty');
                star.textContent = '☆';
            }
        });

        if (!isTemp) {
            currentRating = rating;
            ratingInput.value = rating;
            updateRatingText(rating);
        }
    }

    function updateRatingText(rating) {
        const messages = [
            '별을 드래그하거나 클릭하세요',
            '⭐ 별로예요 (1점)',
            '⭐⭐ 그저 그래요 (2점)',
            '⭐⭐⭐ 괜찮아요 (3점)',
            '⭐⭐⭐⭐ 좋아요! (4점)',
            '⭐⭐⭐⭐⭐ 최고예요! 0원 처리됩니다! (5점)'
        ];

        ratingText.textContent = messages[rating];

        if (rating === 5) {
            ratingText.classList.add('perfect');
        } else {
            ratingText.classList.remove('perfect');
        }
    }

    stars.forEach((star, index) => {
        star.addEventListener('mousedown', () => {
            isMouseDown = true;
            const rating = index + 1;
            updateStars(rating);
        });
    });

    stars.forEach((star, index) => {
        star.addEventListener('mouseenter', () => {
            const rating = index + 1;
            if (isMouseDown) {
                updateStars(rating);
            } else {
                updateStars(rating, true);
            }
        });
    });

    document.addEventListener('mouseup', () => {
        isMouseDown = false;
    });

    document.querySelector('.star-rating').addEventListener('mouseleave', () => {
        if (!isMouseDown) {
            updateStars(currentRating);
        }
    });

    stars.forEach((star, index) => {
        star.addEventListener('click', () => {
            const rating = index + 1;
            updateStars(rating);
        });
    });
}

// ========== 폼 제출 ==========
function initFormSubmit(expenseId) {
    const form = document.getElementById('expenseForm');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const title = document.getElementById('title').value.trim();
        const amount = parseInt(document.getElementById('amount').value);
        const category = document.getElementById('category').value;
        const rating = parseInt(document.getElementById('satisfactionRating').value);
        const purchaseDate = document.getElementById('purchaseDate').value;
        const description = document.getElementById('description').value;

        if (!title) {
            alert('제목을 입력해주세요');
            return;
        }

        if (!category) {
            alert('카테고리를 선택해주세요');
            return;
        }

        if (rating === 0) {
            alert('만족도를 선택해주세요');
            return;
        }

        const data = {
            title,
            amount,
            category,
            satisfactionRating: rating,
            purchaseDate,
            description
        };

        try {
            let response;
            if (expenseId) {
                // 수정 모드
                response = await fetch(`/api/expenses/${expenseId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });
            } else {
                // 등록 모드
                response = await fetch('/api/expenses', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });
            }

            if (response.ok) {
                alert(expenseId ? '수정되었습니다!' : '등록되었습니다!');
                window.location.href = '/';
            } else {
                const error = await response.text();
                alert((expenseId ? '수정' : '등록') + ' 실패: ' + error);
            }
        } catch (error) {
            console.error((expenseId ? '수정' : '등록') + ' 오류:', error);
            alert('오류가 발생했습니다');
        }
    });
}

// ========== 유틸리티 ==========
function formatNumber(num) {
    return num.toLocaleString('ko-KR');
}
