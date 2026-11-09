
export const adminCss = `
.adm { --adm-bg:#f4f6fb; --adm-card:#fff; --adm-text:#1c2433; --adm-muted:#667085; --adm-line:#e4e8f0;
  --adm-brand:#2457d6; --adm-brand-dark:#1b44ad; --adm-ok:#12805c; --adm-warn:#b26a00; --adm-bad:#c0362c;
  display:flex; min-height:100vh; background:var(--adm-bg); color:var(--adm-text); font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif; }
.adm *, .adm *::before, .adm *::after { box-sizing:border-box; }
.adm-side { width:230px; background:#14213d; color:#dfe6f5; padding:18px 12px; display:flex; flex-direction:column; gap:4px; flex-shrink:0; }
.adm-brand { font-weight:700; font-size:1.05rem; padding:6px 10px 16px; color:#fff; }
.adm-link { color:#cfd8ee; text-decoration:none; padding:10px 12px; border-radius:8px; font-size:.95rem; display:block; }
.adm-link:hover { background:rgba(255,255,255,.08); }
.adm-link.active { background:var(--adm-brand); color:#fff; }
.adm-main { flex:1; min-width:0; display:flex; flex-direction:column; }
.adm-top { display:flex; align-items:center; gap:12px; padding:12px 20px; background:var(--adm-card); border-bottom:1px solid var(--adm-line); position:sticky; top:0; z-index:5; }
.adm-top h1 { font-size:1.15rem; margin:0; flex:1; }
.adm-burger { display:none; }
.adm-content { padding:20px; max-width:1300px; width:100%; margin:0 auto; }
.adm-grid { display:grid; gap:14px; grid-template-columns:repeat(auto-fit,minmax(min(170px,100%),1fr)); margin-bottom:18px; }
.adm-card { min-width:0; background:var(--adm-card); border:1px solid var(--adm-line); border-radius:12px; padding:16px; margin-bottom:16px; }
.adm-card h2 { font-size:1rem; margin:0 0 12px; }
.adm-stat { background:var(--adm-card); border:1px solid var(--adm-line); border-radius:12px; padding:14px 16px; }
.adm-stat .v { font-size:1.6rem; font-weight:700; line-height:1.2; }
.adm-stat .l { color:var(--adm-muted); font-size:.85rem; }
.adm-stat .h { color:var(--adm-muted); font-size:.75rem; margin-top:2px; }
.adm-stat.warn .v { color:var(--adm-warn); } .adm-stat.bad .v { color:var(--adm-bad); } .adm-stat.ok .v { color:var(--adm-ok); }
.adm-table-wrap { overflow-x:auto; -webkit-overflow-scrolling:touch; }
.adm table { border-collapse:collapse; width:100%; font-size:.9rem; }
.adm th { text-align:left; color:var(--adm-muted); font-weight:600; font-size:.78rem; text-transform:uppercase; letter-spacing:.03em; padding:8px 10px; border-bottom:1px solid var(--adm-line); white-space:nowrap; }
.adm td { padding:10px; border-bottom:1px solid var(--adm-line); vertical-align:middle; }
.adm tr.sel td { background:#eef3ff; }
.adm tr.clickable { cursor:pointer; } .adm tr.clickable:hover td { background:#f7f9fe; }
.adm-badge { display:inline-block; padding:2px 9px; border-radius:999px; font-size:.75rem; font-weight:600; background:#e8ecf5; color:#38435c; white-space:nowrap; }
.adm-badge.ok { background:#dff5ec; color:var(--adm-ok); } .adm-badge.warn { background:#fff1d6; color:var(--adm-warn); }
.adm-badge.bad { background:#fde3e0; color:var(--adm-bad); } .adm-badge.info { background:#e1ebff; color:var(--adm-brand-dark); }
.adm button, .adm .adm-btn { font:inherit; border:1px solid var(--adm-line); background:#fff; color:var(--adm-text); border-radius:8px; padding:7px 12px; cursor:pointer; }
.adm button:hover:not(:disabled) { border-color:var(--adm-brand); }
.adm button:disabled { opacity:.55; cursor:not-allowed; }
.adm button.primary { background:var(--adm-brand); border-color:var(--adm-brand); color:#fff; }
.adm button.primary:hover:not(:disabled) { background:var(--adm-brand-dark); }
.adm button.danger { color:var(--adm-bad); border-color:#f0bdb8; }
.adm input, .adm select, .adm textarea { font:inherit; padding:8px 10px; border:1px solid #cfd6e4; border-radius:8px; background:#fff; color:var(--adm-text); max-width:100%; }
.adm input:focus, .adm select:focus, .adm textarea:focus { outline:2px solid #b9ccff; border-color:var(--adm-brand); }
.adm label { display:block; font-size:.82rem; color:var(--adm-muted); margin-bottom:4px; }
.adm-form { display:grid; gap:12px; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); }
.adm-form .wide { grid-column:1/-1; }
.adm-form input, .adm-form select, .adm-form textarea { width:100%; }
.adm-row { display:flex; gap:8px; flex-wrap:wrap; align-items:center; }
.adm-toolbar { display:flex; gap:10px; flex-wrap:wrap; align-items:center; margin-bottom:12px; }
.adm-tabs { display:flex; gap:6px; margin-bottom:14px; flex-wrap:wrap; }
.adm-tabs button.on { background:#14213d; color:#fff; border-color:#14213d; }
.adm-note { padding:10px 12px; border-radius:8px; margin-bottom:12px; font-size:.9rem; }
.adm-note.err { background:#fde3e0; color:#8a241c; } .adm-note.okk { background:#dff5ec; color:#0d5f44; } .adm-note.inf { background:#e1ebff; color:#1b3d91; }
.adm-muted { color:var(--adm-muted); font-size:.85rem; }
.adm-two { display:grid; gap:16px; grid-template-columns:minmax(0,1.5fr) minmax(0,1fr); align-items:start; }
.adm-bar { display:flex; align-items:center; gap:10px; margin:6px 0; font-size:.88rem; }
.adm-bar .name { width:110px; flex-shrink:0; } .adm-bar .track { flex:1; background:#edf0f7; border-radius:6px; height:12px; overflow:hidden; }
.adm-bar .fill { height:100%; background:var(--adm-brand); border-radius:6px; } .adm-bar .num { width:40px; text-align:right; font-weight:600; }
.adm-thumb { width:44px; height:44px; border-radius:8px; object-fit:cover; background:#e8ecf5; display:block; }
.adm-list { list-style:none; margin:0; padding:0; } .adm-list li { padding:8px 0; border-bottom:1px solid var(--adm-line); display:flex; gap:10px; justify-content:space-between; font-size:.9rem; }
@media (max-width:900px) {
  .adm-side { position:fixed; inset:0 auto 0 0; z-index:20; transform:translateX(-100%); transition:transform .2s; width:250px; }
  .adm-side.open { transform:translateX(0); box-shadow:4px 0 24px rgba(0,0,0,.25); }
  .adm-burger { display:inline-block; }
  .adm-two { grid-template-columns:minmax(0,1fr); }
  .adm-top .adm-muted { display:none; }
  .adm-top h1 { font-size:1rem; min-width:0; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
  .adm-content { padding:14px; }
  .adm-scrim { position:fixed; inset:0; background:rgba(0,0,0,.35); z-index:15; }
}
`;