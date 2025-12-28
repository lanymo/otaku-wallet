// ========== 대시보드 - 통계 로드 ==========
async function loadStatistics() {
    try {
        const response = await fetch('/api/expenses/statistics');
        const stats = await response.json();

        document.getElementById('totalAmount').textContent =
            formatNumber(stats.totalAmount) + '원';
        document.getElementById('displayAmount').textContent =
            formatNumber(stats.displayAmount) + '원';
        document.getElementById('savedAmount').textContent =
            formatNumber(stats.savedAmount) + '원';
        document.getElementById('satisfiedCount').textContent =
            stats.satisfiedCount + '개';
    } catch (error) {
        console.error('통계 로드 실패:', error);
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

        listContainer.innerHTML = expenses.map(expense => `
            <div class="expense-card ${expense.isSatisfied ? 'satisfied' : ''}">
                <div class="expense-info">
                    <div class="expense-header">
                        <span class="expense-category">${expense.categoryEmoji}</span>
                        <span class="expense-category-name">${expense.category}</span>
                    </div>
                    <div class="expense-amount ${expense.displayAmount === 0 ? 'zero' : ''}">
                        ${formatNumber(expense.displayAmount)}원
                        ${expense.displayAmount !== expense.amount ?
            `<span class="expense-original">${formatNumber(expense.amount)}원</span>`
            : ''}
                    </div>
                    <div class="expense-stars">${'★'.repeat(expense.satisfactionRating)}${'☆'.repeat(5 - expense.satisfactionRating)}</div>
                    <div class="expense-description">${expense.description || ''}</div>
                    <div class="expense-date">${expense.purchaseDate}</div>
                </div>
                <div class="expense-actions">
                    <button class="btn btn-danger" onclick="deleteExpense(${expense.id})">삭제</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('지출 목록 로드 실패:', error);
        document.getElementById('expenseList').innerHTML =
            '<div class="empty">데이터를 불러올 수 없습니다</div>';
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

// ========== 폼 초기화 ==========
function initForm() {
    // 오늘 날짜 기본값
    document.getElementById('purchaseDate').valueAsDate = new Date();

    // 카테고리 선택
    initCategorySelection();

    // 별점 기능
    initStarRating();

    // 폼 제출
    initFormSubmit();
}

// ========== 카테고리 선택 ==========
function initCategorySelection() {
    const categoryBtns = document.querySelectorAll('.category-btn');
    const categoryInput = document.getElementById('category');

    categoryBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // 모든 버튼에서 selected 제거
            categoryBtns.forEach(b => b.classList.remove('selected'));

            // 클릭한 버튼 selected 추가
            this.classList.add('selected');

            // hidden input에 값 설정
            categoryInput.value = this.getAttribute('data-category');
        });
    });
}

// ========== 별점 기능 (드래그 + 클릭) ==========
function initStarRating() {
    const stars = document.querySelectorAll('.star');
    const ratingInput = document.getElementById('satisfactionRating');
    const ratingText = document.getElementById('ratingText');

    let currentRating = 0;
    let isMouseDown = false;

    // 별 상태 업데이트
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

    // 별점 텍스트 업데이트
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

    // 마우스 다운
    stars.forEach((star, index) => {
        star.addEventListener('mousedown', () => {
            isMouseDown = true;
            const rating = index + 1;
            updateStars(rating);
        });
    });

    // 마우스 이동 (드래그)
    stars.forEach((star, index) => {
        star.addEventListener('mouseenter', () => {
            const rating = index + 1;
            if (isMouseDown) {
                updateStars(rating);
            } else {
                updateStars(rating, true); // 임시 미리보기
            }
        });
    });

    // 마우스 업
    document.addEventListener('mouseup', () => {
        isMouseDown = false;
    });

    // 마우스가 별점 영역을 벗어났을 때
    document.querySelector('.star-rating').addEventListener('mouseleave', () => {
        if (!isMouseDown) {
            updateStars(currentRating);
        }
    });

    // 클릭 (한 번에 고정)
    stars.forEach((star, index) => {
        star.addEventListener('click', () => {
            const rating = index + 1;
            updateStars(rating);
        });
    });
}

// ========== 폼 제출 ==========
function initFormSubmit() {
    const form = document.getElementById('expenseForm');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // 데이터 수집
        const amount = parseInt(document.getElementById('amount').value);
        const category = document.getElementById('category').value;
        const rating = parseInt(document.getElementById('satisfactionRating').value);
        const purchaseDate = document.getElementById('purchaseDate').value;
        const description = document.getElementById('description').value;

        // 유효성 검사
        if (!category) {
            alert('카테고리를 선택해주세요');
            return;
        }

        if (rating === 0) {
            alert('만족도를 선택해주세요');
            return;
        }

        const data = {
            amount,
            category,
            satisfactionRating: rating,
            purchaseDate,
            description
        };

        try {
            const response = await fetch('/api/expenses', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                alert('등록되었습니다!');
                window.location.href = '/'; // 대시보드로 이동
            } else {
                const error = await response.text();
                alert('등록 실패: ' + error);
            }
        } catch (error) {
            console.error('등록 오류:', error);
            alert('오류가 발생했습니다');
        }
    });
}

// ========== 유틸리티 - 숫자 포맷 ==========
function formatNumber(num) {
    return num.toLocaleString('ko-KR');
}