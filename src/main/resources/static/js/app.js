// app.js - telas do app: Início, Ações, Recompensas, Ranking e Histórico.

// Protege a página: sem token, volta para o login
if (!getToken()) window.location.href = "index.html";

// ---------- Estado em memória ----------
const state = { me: null, ranking: null, history: null, actions: [], rewards: [], vouchers: [] };

// ---------- Utilitários ----------
const $ = (id) => document.getElementById(id);
function el(tag, cls, text) {
  const e = document.createElement(tag);
  if (cls) e.className = cls;
  if (text !== undefined) e.textContent = text; // textContent evita injeção de HTML
  return e;
}
function toast(msg, isErr) {
  const t = $("toast");
  t.textContent = msg;
  t.className = "toast show" + (isErr ? " err" : "");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => (t.className = "toast"), 3200);
}
// Pega o primeiro campo que existir (aceita caminhos como "action.name")
function pick(obj, keys, fallback) {
  for (const k of keys) {
    let v = obj;
    for (const part of k.split(".")) v = v == null ? undefined : v[part];
    if (v !== undefined && v !== null && v !== "") return v;
  }
  return fallback;
}
function fmtDate(v) {
  if (!v) return "";
  const d = new Date(v);
  return isNaN(d) ? String(v) : d.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}
const CAT_ICON = { "Reciclagem": "♻️", "Mobilidade": "🚲", "Água": "💧", "Energia": "⚡", "Meio ambiente": "🌳" };
const REWARD_ICON = { 1: "🎁", 2: "🌱", 3: "🎟️" };

// ---------- Carregar dados da API ----------
async function loadAll() {
  const [me, ranking, history, actions, rewards, vouchers] = await Promise.all([
    apiFetch("/api/users/me"),
    apiFetch("/api/ranking"),
    apiFetch("/api/history"),
    apiFetch("/api/actions"),
    apiFetch("/api/rewards"),
    apiFetch("/api/vouchers"),
  ]);
  if (me.ok) {
    state.me = me.data;
    const u = getUser() || {};
    localStorage.setItem("ecopontos_user", JSON.stringify({ ...u, ...me.data }));
  } else state.me = getUser();
  state.ranking = ranking.ok ? ranking.data : null;
  state.history = history.ok ? history.data : { actions: [], redemptions: [], transactions: [] };
  state.actions = actions.ok && Array.isArray(actions.data) ? actions.data : [];
  state.rewards = rewards.ok && Array.isArray(rewards.data) ? rewards.data : [];
  state.vouchers = vouchers.ok && Array.isArray(vouchers.data) ? vouchers.data : [];
  renderAll();
}
function myRole() { return (state.me && state.me.role) || "USER"; }
function canValidate() { return myRole() === "PARTNER" || myRole() === "ADMIN"; }
function myPoints() { return (state.me && state.me.points) || 0; }
function myLevel() {
  const list = state.ranking && state.ranking.ranking;
  const pos = state.ranking && state.ranking.yourPosition;
  if (list && pos) { const mine = list.find((r) => r.position === pos); if (mine) return mine.level; }
  return "Iniciante";
}

// ---------- Renderização ----------
function renderAll() {
  $("top-points").textContent = myPoints();
  $("nav-validate").classList.toggle("hidden", !canValidate());
  $("nav-approvals").classList.toggle("hidden", myRole() !== "ADMIN");
  renderHome(); renderActions(); renderRewards(); renderRanking(); renderVouchers(); renderHistory();
  if (canValidate() && !$("validate-input")) renderValidate();
}

function renderHome() {
  const v = $("view-home"); v.replaceChildren();
  const card = el("div", "points-card");
  card.append(el("div", "lbl", "Olá, " + ((state.me && state.me.name) || "") + "!"));
  const num = el("div", "num"); num.textContent = myPoints() + " pts"; card.append(num);
  card.append(el("span", "badge", "🌿 " + myLevel()));
  v.append(card);

  const h = state.history || {};
  const stats = el("div", "stats");
  const pos = state.ranking && state.ranking.yourPosition;
  [[(h.actions || []).filter((x) => !x.status || x.status === "APPROVED").length, "Ações feitas"], [(h.redemptions || []).length, "Resgates"], [pos ? "#" + pos : "-", "No ranking"]]
    .forEach(([val, lab]) => { const s = el("div", "stat"); s.append(el("div", "v", String(val)), el("div", "l", lab)); stats.append(s); });
  v.append(stats);

  v.append(el("div", "sec-title", "Últimas ações"));
  const recent = (h.actions || []).slice(0, 3);
  if (!recent.length) {
    const e = el("div", "empty", "Você ainda não registrou nenhuma ação. Que tal começar agora?");
    const b = el("button", "btn", "Ver ações sustentáveis"); b.style.marginTop = "12px"; b.style.maxWidth = "260px";
    b.onclick = () => showView("actions"); e.append(b); v.append(e);
  } else {
    const list = el("div", "list"); recent.forEach((a) => list.append(historyActionItem(a))); v.append(list);
  }
}

// Limite diário: essa ação já foi registrada hoje?
function doneToday(a) {
  const today = new Date().toDateString();
  return ((state.history && state.history.actions) || []).some((h) =>
    h.status !== "REJECTED" && pick(h, ["actionName", "action.name"], "") === a.name && new Date(pick(h, ["date", "createdAt"], 0)).toDateString() === today);
}

function renderActions() {
  const v = $("view-actions"); v.replaceChildren();
  v.append(el("h2", null, "Ações sustentáveis"), el("p", "muted", "Registre uma ação e ganhe pontos na hora."));
  const list = el("div", "list");
  state.actions.filter((a) => a.active !== false).forEach((a) => {
    const item = el("div", "item");
    item.append(el("div", "ic", CAT_ICON[a.category] || "🌿"));
    const body = el("div", "body"); body.append(el("div", "n", a.name), el("div", "d", a.description + (a.requiresProof ? " · 📷 exige foto" : "")));
    item.append(body, el("span", "tag", "+" + a.points + " pts"));
    const done = doneToday(a);
    const btn = el("button", "btn" + (done ? " off" : ""), done ? "Feita hoje ✓" : "Registrar");
    btn.onclick = () => { if (done) toast("Você já registrou essa ação hoje. Volte amanhã!", true); else registerAction(a, btn); };
    item.append(btn); list.append(item);
  });
  if (!state.actions.length) list.append(el("div", "empty", "Nenhuma ação disponível no momento."));
  v.append(list);
}

function renderRewards() {
  const v = $("view-rewards"); v.replaceChildren();
  v.append(el("h2", null, "Recompensas"), el("p", "muted", "Você tem " + myPoints() + " pontos para trocar."));
  const list = el("div", "list");
  state.rewards.filter((r) => r.active !== false).forEach((r) => {
    const item = el("div", "item");
    item.append(el("div", "ic", REWARD_ICON[r.id] || "🎁"));
    const body = el("div", "body");
    body.append(el("div", "n", r.name), el("div", "d", r.description + " · Estoque: " + r.stock));
    item.append(body, el("span", "tag amber", r.pointsRequired + " pts"));
    const noStock = r.stock <= 0, noPoints = myPoints() < r.pointsRequired;
    const btn = el("button", "btn" + (noStock || noPoints ? " off" : ""), noStock ? "Esgotado" : "Resgatar");
    btn.onclick = () => redeemReward(r, btn);
    item.append(btn); list.append(item);
  });
  if (!state.rewards.length) list.append(el("div", "empty", "Nenhuma recompensa disponível."));
  v.append(list);
}

function renderRanking() {
  const v = $("view-ranking"); v.replaceChildren();
  v.append(el("h2", null, "Ranking"));
  const data = state.ranking;
  if (!data || !data.ranking || !data.ranking.length) { v.append(el("div", "empty", "Ranking indisponível.")); return; }
  v.append(el("p", "muted", "O ranking conta os pontos acumulados: gastar pontos em recompensas não faz você cair."));
  if (data.yourPosition) v.append(el("p", "muted", "Você está na posição #" + data.yourPosition + "."));
  const list = el("div", "list");
  data.ranking.forEach((r) => {
    const item = el("div", "item rank-row" + (r.position === data.yourPosition ? " me" : ""));
    item.append(el("div", "pos" + (r.position === 1 ? " p1" : ""), r.position === 1 ? "🥇" : String(r.position)));
    const body = el("div", "body"); body.append(el("div", "n", r.name), el("div", "d", r.level));
    item.append(body, el("span", "tag", r.points + " pts")); list.append(item);
  });
  v.append(list);
}

// ---------- Vouchers ----------
function renderVouchers() {
  const v = $("view-vouchers"); v.replaceChildren();
  v.append(el("h2", null, "Meus vouchers"), el("p", "muted", "Mostre o código na loja parceira para receber sua recompensa."));
  const list = el("div", "list");
  state.vouchers.forEach((vc) => {
    const used = vc.status === "USED";
    const box = el("div", "voucher" + (used ? " used" : ""));
    const top = el("div", "top");
    const info = el("div"); info.append(el("div", "n", vc.rewardName), el("div", "d", "Resgatado em " + fmtDate(vc.createdAt)));
    top.append(info, el("span", "tag" + (used ? " gray" : ""), used ? "Usado" : "Ativo"));
    box.append(top, el("div", "code", vc.code));
    if (used) box.append(el("div", "d", "Usado em " + fmtDate(vc.usedAt)));
    else {
      const cp = el("button", "btn ghost", "Copiar código");
      cp.onclick = () => copyText(vc.code);
      box.append(cp);
    }
    list.append(box);
  });
  if (!state.vouchers.length) list.append(el("div", "empty", "Você ainda não tem vouchers. Resgate uma recompensa para receber o seu!"));
  v.append(list);
}
function copyText(text) {
  if (navigator.clipboard) navigator.clipboard.writeText(text).then(() => toast("Código copiado!"), () => toast(text));
  else toast(text);
}

// ---------- Validação (lojas parceiras e admin) ----------
function renderValidate() {
  const v = $("view-validate"); v.replaceChildren();
  v.append(el("h2", null, "Validar voucher"), el("p", "muted", "Digite o código que o cliente mostrou e confirme a entrega."));
  const input = el("input"); input.id = "validate-input"; input.placeholder = "ECO-XXXX-XXXX"; input.maxLength = 20;
  input.autocomplete = "off"; input.style.textTransform = "uppercase";
  const btn = el("button", "btn", "Consultar voucher");
  const result = el("div");
  btn.onclick = () => lookupVoucher(input.value, result, btn);
  input.addEventListener("keydown", (e) => { if (e.key === "Enter") btn.click(); });
  v.append(input, btn, result);
}
async function lookupVoucher(code, box, btn) {
  box.replaceChildren();
  code = code.trim().toUpperCase();
  if (!code) { toast("Digite o código do voucher.", true); return; }
  btn.disabled = true;
  const r = await apiFetch("/api/vouchers/" + encodeURIComponent(code));
  btn.disabled = false;
  if (!r.ok) { box.append(el("div", "empty", errorMessage(r.data))); return; }
  showVoucherResult(r.data, box);
}
function showVoucherResult(vc, box) {
  box.replaceChildren();
  const used = vc.status === "USED";
  const card = el("div", "validate-box");
  [["Código", vc.code], ["Recompensa", vc.rewardName], ["Cliente", vc.userName], ["Resgatado em", fmtDate(vc.createdAt)],
   ["Situação", used ? "Já usado em " + fmtDate(vc.usedAt) : "Válido"]].forEach(([k, val]) => {
    const row = el("div", "row"); row.append(el("span", null, k), el("span", null, String(val))); card.append(row);
  });
  if (!used) {
    const use = el("button", "btn", "Confirmar entrega e marcar como usado"); use.style.marginTop = "14px";
    use.onclick = async () => {
      use.disabled = true;
      const r = await apiFetch("/api/vouchers/" + encodeURIComponent(vc.code) + "/use", { method: "POST" });
      if (!r.ok) { use.disabled = false; toast(errorMessage(r.data), true); return; }
      toast("Voucher marcado como usado ✓");
      showVoucherResult(r.data, box);
    };
    card.append(use);
  } else {
    const note = el("div", "d", "Este voucher não pode ser usado de novo."); note.style.marginTop = "10px"; card.append(note);
  }
  box.append(card);
}

function historyActionItem(a) {
  const item = el("div", "item");
  const name = pick(a, ["actionName", "action.name", "name", "sustainableAction.name", "description"], "Ação registrada");
  const pts = pick(a, ["points", "pointsEarned", "pointsAwarded", "action.points"], null);
  const when = pick(a, ["createdAt", "registeredAt", "performedAt", "date", "timestamp"], "");
  const cat = pick(a, ["category", "action.category"], "");
  item.append(el("div", "ic", CAT_ICON[cat] || "✅"));
  const body = el("div", "body"); body.append(el("div", "n", name), el("div", "d", fmtDate(when)));
  if (a.status === "REJECTED" && a.reviewNote) body.append(el("div", "note", "Motivo: " + a.reviewNote));
  item.append(body);
  if (a.status === "PENDING") item.append(el("span", "tag amber", "⏳ Em análise"));
  else if (a.status === "REJECTED") item.append(el("span", "tag red", "Rejeitada"));
  else if (pts !== null) item.append(el("span", "tag", "+" + pts + " pts"));
  return item;
}

function renderHistory() {
  const v = $("view-history"); v.replaceChildren();
  v.append(el("h2", null, "Histórico"));
  const h = state.history || {};
  v.append(el("div", "sec-title", "Ações registradas"));
  const acts = el("div", "list");
  (h.actions || []).forEach((a) => acts.append(historyActionItem(a)));
  if (!(h.actions || []).length) acts.append(el("div", "empty", "Nenhuma ação registrada ainda."));
  v.append(acts);

  v.append(el("div", "sec-title", "Resgates"));
  const reds = el("div", "list");
  (h.redemptions || []).forEach((r) => {
    const item = el("div", "item");
    const name = pick(r, ["rewardName", "reward.name", "name"], "Recompensa resgatada");
    const pts = pick(r, ["pointsSpent", "pointsUsed", "points", "reward.pointsRequired"], null);
    item.append(el("div", "ic", "🎁"));
    const body = el("div", "body");
    body.append(el("div", "n", name), el("div", "d", fmtDate(pick(r, ["createdAt", "redeemedAt", "date", "timestamp"], ""))));
    item.append(body);
    if (pts !== null) item.append(el("span", "tag amber", "-" + Math.abs(pts) + " pts"));
    reds.append(item);
  });
  if (!(h.redemptions || []).length) reds.append(el("div", "empty", "Nenhum resgate ainda."));
  v.append(reds);

  v.append(el("div", "sec-title", "Extrato de pontos"));
  const tr = el("div", "list");
  (h.transactions || []).forEach((t) => {
    const item = el("div", "item");
    const amount = Number(pick(t, ["points", "amount", "value"], 0));
    item.append(el("div", "ic", amount >= 0 ? "➕" : "➖"));
    const body = el("div", "body");
    body.append(el("div", "n", pick(t, ["description", "type", "reason"], "Movimentação")), el("div", "d", fmtDate(pick(t, ["createdAt", "date", "timestamp"], ""))));
    item.append(body, el("span", "tag" + (amount < 0 ? " amber" : ""), (amount >= 0 ? "+" : "") + amount + " pts"));
    tr.append(item);
  });
  if (!(h.transactions || []).length) tr.append(el("div", "empty", "Sem movimentações ainda."));
  v.append(tr);
}

// ---------- Ações do usuário ----------
async function registerAction(a, btn) {
  if (a.requiresProof) {
    const file = await pickPhoto();
    if (!file) return; // pessoa cancelou
    btn.disabled = true;
    const form = new FormData();
    form.append("photo", await shrinkImage(file), "comprovacao.jpg");
    const r = await apiFetch("/api/actions/" + a.id + "/register", { method: "POST", body: form });
    btn.disabled = false;
    if (!r.ok) { toast(errorMessage(r.data), true); return; }
    toast("Foto enviada! Os pontos entram quando um administrador aprovar ⏳");
    await loadAll();
    return;
  }
  btn.disabled = true;
  const r = await apiFetch("/api/actions/" + a.id + "/register", { method: "POST" });
  btn.disabled = false;
  if (!r.ok) { toast(errorMessage(r.data), true); return; }
  toast("+" + a.points + " pontos! " + a.name + " registrada 🌱");
  await loadAll();
}

// Abre a câmera/galeria do aparelho e devolve o arquivo escolhido
function pickPhoto() {
  return new Promise((resolve) => {
    const input = document.createElement("input");
    input.type = "file"; input.accept = "image/*"; input.capture = "environment";
    input.onchange = () => resolve(input.files && input.files[0] ? input.files[0] : null);
    input.oncancel = () => resolve(null);
    input.click();
  });
}
// Reduz a foto (lado maior 1280px, JPEG) para o envio ser rápido e leve
function shrinkImage(file) {
  return new Promise((resolve) => {
    const img = new Image();
    const url = URL.createObjectURL(file);
    img.onload = () => {
      const scale = Math.min(1, 1280 / Math.max(img.width, img.height));
      const c = document.createElement("canvas");
      c.width = Math.round(img.width * scale); c.height = Math.round(img.height * scale);
      c.getContext("2d").drawImage(img, 0, 0, c.width, c.height);
      URL.revokeObjectURL(url);
      c.toBlob((b) => resolve(b || file), "image/jpeg", 0.82);
    };
    img.onerror = () => { URL.revokeObjectURL(url); resolve(file); };
    img.src = url;
  });
}

// ---------- Aprovações (admin) ----------
async function renderApprovals() {
  const v = $("view-approvals"); v.replaceChildren();
  v.append(el("h2", null, "Aprovações pendentes"), el("p", "muted", "Confira a foto e aprove para creditar os pontos."));
  const r = await apiFetch("/api/admin/proofs?status=PENDING");
  if (!r.ok) { v.append(el("div", "empty", errorMessage(r.data))); return; }
  const list = el("div", "list");
  r.data.forEach((p) => {
    const box = el("div", "proof");
    const top = el("div", "top");
    const info = el("div"); info.append(el("div", "n", p.actionName), el("div", "d", p.userName + " · " + fmtDate(p.createdAt)));
    top.append(info, el("span", "tag", "+" + p.points + " pts"));
    top.style.display = "flex"; top.style.justifyContent = "space-between"; top.style.gap = "10px";
    const ph = el("div", "photo-ph", "Carregando foto...");
    box.append(top, ph);
    apiBlob("/api/proofs/" + p.id + "/photo").then((blob) => {
      if (!blob) { ph.textContent = "Foto indisponível"; return; }
      const img = el("img", "photo"); img.alt = "Comprovação de " + p.userName; img.src = URL.createObjectURL(blob);
      ph.replaceWith(img);
    });
    const actions = el("div", "actions");
    const ok = el("button", "btn", "Aprovar");
    const no = el("button", "btn danger", "Rejeitar");
    ok.onclick = async () => {
      ok.disabled = no.disabled = true;
      const res = await apiFetch("/api/admin/proofs/" + p.id + "/approve", { method: "POST" });
      if (!res.ok) { ok.disabled = no.disabled = false; toast(errorMessage(res.data), true); return; }
      toast("Aprovada! +" + p.points + " pts para " + p.userName);
      renderApprovals();
    };
    no.onclick = async () => {
      const note = prompt("Motivo da rejeição (opcional):");
      if (note === null) return;
      ok.disabled = no.disabled = true;
      const res = await apiFetch("/api/admin/proofs/" + p.id + "/reject", { method: "POST", body: { note } });
      if (!res.ok) { ok.disabled = no.disabled = false; toast(errorMessage(res.data), true); return; }
      toast("Ação rejeitada.");
      renderApprovals();
    };
    actions.append(ok, no); box.append(actions); list.append(box);
  });
  if (!r.data.length) list.append(el("div", "empty", "Nenhuma ação aguardando aprovação. 🎉"));
  v.append(list);
}

async function redeemReward(rw, btn) {
  if (rw.stock <= 0) { toast("Recompensa esgotada.", true); return; }
  if (myPoints() < rw.pointsRequired) { toast("Pontos insuficientes: faltam " + (rw.pointsRequired - myPoints()) + " pts.", true); return; }
  if (!confirm("Resgatar \"" + rw.name + "\" por " + rw.pointsRequired + " pontos?")) return;
  btn.disabled = true;
  const r = await apiFetch("/api/rewards/" + rw.id + "/redeem", { method: "POST" });
  btn.disabled = false;
  if (!r.ok) { toast(errorMessage(r.data), true); return; }
  toast("Resgate realizado! Seu código: " + (r.data && r.data.voucherCode ? r.data.voucherCode : "veja em Vouchers") + " 🎉");
  await loadAll();
  showView("vouchers");
}

// ---------- Navegação entre telas ----------
function showView(name) {
  document.querySelectorAll(".view").forEach((s) => s.classList.toggle("hidden", s.id !== "view-" + name));
  document.querySelectorAll("#nav button").forEach((b) => b.classList.toggle("active", b.dataset.view === name));
  window.scrollTo(0, 0);
  if (name === "approvals") renderApprovals();
}
$("nav").addEventListener("click", (e) => { const b = e.target.closest("button"); if (b) showView(b.dataset.view); });

loadAll();
