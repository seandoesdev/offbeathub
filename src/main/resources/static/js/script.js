document.querySelector('.search-bar').addEventListener('input', function(e) {
    const query = e.target.value.toLowerCase();
    document.querySelectorAll('.art-card').forEach(card => {
        const title = card.querySelector('.title').textContent.toLowerCase();
        const author = card.querySelector('.author').textContent.toLowerCase();
        card.style.display = (title.includes(query) || author.includes(query)) ? '' : 'none';
    });
});

// 간단한 회원가입 입력값 검증 예시
const signupSubmit = document.getElementById('signupSubmit');
signupSubmit.onclick = function() {
  const user = document.getElementById('signupUsername').value;
  const pw = document.getElementById('signupPassword').value;
  const pw2 = document.getElementById('signupPassword2').value;
  if (!user || !pw || !pw2) { alert('모든 항목을 입력하세요.'); return; }
  if (pw !== pw2) { alert('비밀번호가 일치하지 않습니다.'); return; }
  alert('회원가입이 완료되었습니다! (실제 저장은 백엔드 필요)');
  signupModal.style.display = 'none';
};
// 간단한 로그인 예시
const loginSubmit = document.getElementById('loginSubmit');
loginSubmit.onclick = function() {
  const user = document.getElementById('loginUsername').value;
  const pw = document.getElementById('loginPassword').value;
  if (!user || !pw) { alert('아이디와 비밀번호를 입력하세요.'); return; }
  alert('로그인 시도! (실제 인증은 백엔드 필요)');
  loginModal.style.display = 'none';
};

