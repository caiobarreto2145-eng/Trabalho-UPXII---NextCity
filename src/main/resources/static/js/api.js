// api.js - funções compartilhadas: guarda o token e fala com a API.
// A API roda no mesmo endereço da página (localhost:8080), então os caminhos são relativos.

const TOKEN_KEY = "ecopontos_token";
const USER_KEY = "ecopontos_user";

function saveSession(data) {
  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(USER_KEY, JSON.stringify({
    id: data.id, name: data.name, email: data.email, points: data.points
  }));
}
function getToken() { return localStorage.getItem(TOKEN_KEY); }
function getUser() {
  try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch (e) { return null; }
}
function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}
function logout() { clearSession(); window.location.href = "index.html"; }

// Transforma a resposta de erro da API em um texto para mostrar ao usuário.
// A API devolve {"error": "..."} ou {"campo": "mensagem", ...}.
function errorMessage(data) {
  if (!data) return "Não foi possível conectar ao servidor.";
  if (typeof data.error === "string") return data.error;
  const vals = Object.values(data).filter(v => typeof v === "string");
  return vals.length ? vals.join(" ") : "Ocorreu um erro. Tente novamente.";
}

// Chamada genérica à API. Se auth=true, envia o token JWT.
async function apiFetch(path, { method = "GET", body = null, auth = true } = {}) {
  const headers = {};
  // FormData (envio de foto) define o Content-Type sozinho; JSON precisa do cabeçalho
  const isForm = typeof FormData !== "undefined" && body instanceof FormData;
  if (!isForm) headers["Content-Type"] = "application/json";
  if (auth && getToken()) headers["Authorization"] = "Bearer " + getToken();

  let res;
  try {
    res = await fetch(path, { method, headers, body: body ? (isForm ? body : JSON.stringify(body)) : null });
  } catch (e) {
    return { ok: false, status: 0, data: null };
  }
  let data = null;
  try { data = await res.json(); } catch (e) { /* resposta sem JSON */ }

  // Token vencido ou inválido em rota protegida: volta para o login
  if (auth && (res.status === 401 || res.status === 403)) {
    clearSession();
    window.location.href = "index.html";
  }
  return { ok: res.ok, status: res.status, data };
}

// Baixa um arquivo protegido (ex.: foto de comprovação) como Blob; <img> não consegue enviar o token sozinho.
async function apiBlob(path) {
  try {
    const res = await fetch(path, { headers: { "Authorization": "Bearer " + getToken() } });
    return res.ok ? await res.blob() : null;
  } catch (e) { return null; }
}
