// auth.js - lógica da tela de entrar / criar conta (index.html)

// Se já está logado, vai direto para o app
if (getToken()) window.location.href = "app.html";

const tabLogin = document.getElementById("tab-login");
const tabRegister = document.getElementById("tab-register");
const formLogin = document.getElementById("form-login");
const formRegister = document.getElementById("form-register");
const msgBox = document.getElementById("msg");

function showMsg(text, type) {
  msgBox.textContent = text;
  msgBox.className = "msg " + type;
}
function clearMsg() { msgBox.className = "msg"; msgBox.textContent = ""; }

function showTab(which) {
  const isLogin = which === "login";
  tabLogin.classList.toggle("active", isLogin);
  tabRegister.classList.toggle("active", !isLogin);
  formLogin.classList.toggle("hidden", !isLogin);
  formRegister.classList.toggle("hidden", isLogin);
  clearMsg();
}
tabLogin.addEventListener("click", () => showTab("login"));
tabRegister.addEventListener("click", () => showTab("register"));

// Envia e-mail e senha para /api/auth/login e guarda o token
async function doLogin(email, password) {
  // >>> Se o seu LoginRequest usa outros nomes de campo, troque aqui <<<
  const r = await apiFetch("/api/auth/login", { method: "POST", auth: false, body: { email, password } });
  if (!r.ok) return errorMessage(r.data);
  saveSession(r.data);
  window.location.href = "app.html";
  return null;
}

formLogin.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearMsg();
  const btn = formLogin.querySelector("button[type=submit]");
  btn.disabled = true;
  const err = await doLogin(
    document.getElementById("login-email").value.trim(),
    document.getElementById("login-password").value
  );
  if (err) { showMsg(err, "error"); btn.disabled = false; }
});

formRegister.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearMsg();
  const btn = formRegister.querySelector("button[type=submit]");
  btn.disabled = true;
  const email = document.getElementById("reg-email").value.trim();
  const password = document.getElementById("reg-password").value;
  const password2 = document.getElementById("reg-password2").value;
  if (password !== password2) {
    showMsg("As senhas não conferem.", "error");
    btn.disabled = false;
    return;
  }

  // >>> Se o seu RegisterRequest usa outros nomes de campo, troque aqui <<<
  const r = await apiFetch("/api/auth/register", {
    method: "POST", auth: false,
    body: {
      name: document.getElementById("reg-name").value.trim(), email, password,
      // nome do campo de confirmação: confira no seu RegisterRequest (dto)
      confirmPassword: password2, passwordConfirmation: password2
    }
  });
  if (!r.ok) { showMsg(errorMessage(r.data), "error"); btn.disabled = false; return; }

  // Cadastro ok: já entra automaticamente
  showMsg("Conta criada! Entrando...", "ok");
  const err = await doLogin(email, password);
  if (err) { showMsg(err, "error"); btn.disabled = false; }
});
