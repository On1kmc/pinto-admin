'use strict';

// ── Accounts link: show modal for non-MAIN users ─────────────────────────────
(function () {
    var link = document.getElementById('accountsLink');
    if (!link || link.dataset.accountsAllowed === 'true') return;
    link.addEventListener('click', function (e) {
        e.preventDefault();
        new bootstrap.Modal(document.getElementById('accessDeniedModal')).show();
    });
}());

// ── Search page: require at least one field ───────────────────────────────────
(function () {
    var form = document.getElementById('searchForm');
    if (!form) return;
    form.addEventListener('submit', function (e) {
        var id    = (document.getElementById('searchId')       || {}).value || '';
        var uname = (document.getElementById('searchUsername') || {}).value || '';
        if (!id.trim() && !uname.trim()) {
            e.preventDefault();
            toast('Введите ID или Username для поиска', 'warn');
        }
    });
}());

// ── Show-user page: AJAX actions ──────────────────────────────────────────────
(function () {
    var hero = document.querySelector('.user-hero[data-user-id]');
    if (!hero) return;

    var userId  = hero.dataset.userId;
    var isAdmin = hero.dataset.isAdmin === 'true';
    var rightsLoaded = false;

    // ── Rights ────────────────────────────────────────────────────────────────

    function loadRights() {
        if (rightsLoaded) return;
        var container = document.getElementById('rightsList');
        if (!container) return;

        // data-editable выставляется контроллером через Authentication (надёжнее sec:authorize в JS)
        var editable = document.getElementById('rightsBlock').dataset.editable === 'true';
        // currentRights задаётся Thymeleaf inline-скриптом в show-user.html
        var active = Array.isArray(window.currentRights) ? window.currentRights : [];

        fetch('/api/rightslist')
            .then(function (r) { return r.json(); })
            .then(function (rights) {
                rightsLoaded = true;
                var keys = Object.keys(rights);
                if (keys.length === 0) {
                    container.innerHTML = '<span style="color:var(--muted);font-size:13px;">Нет доступных прав</span>';
                    return;
                }
                container.innerHTML = keys.map(function (key) {
                    var checked   = active.indexOf(key) !== -1 ? ' checked' : '';
                    var disabled  = editable ? '' : ' disabled';
                    var cursor    = editable ? 'pointer' : 'default';
                    return '<label style="display:flex;align-items:center;gap:8px;font-size:13px;cursor:' + cursor + ';">'
                        + '<input type="checkbox" class="rights-cb form-check-input"'
                        + ' value="' + esc(key) + '"' + checked + disabled + ' style="margin:0;">'
                        + '<span>' + esc(rights[key]) + '</span>'
                        + '</label>';
                }).join('');

                if (!editable) {
                    // Блокируем все клики на уровне контейнера — надёжнее одного disabled
                    container.style.opacity = '0.55';
                    container.style.pointerEvents = 'none';
                    container.style.userSelect = 'none';
                    var info = document.getElementById('rightsInfo');
                    if (info) info.innerHTML =
                        '<i class="bi bi-eye me-1"></i>Просмотр прав. Изменение доступно только главным администраторам.';
                }
            })
            .catch(function () {
                var c = document.getElementById('rightsList');
                if (c) c.innerHTML = '<span style="color:#ef4444;font-size:13px;">Ошибка загрузки прав</span>';
            });
    }

    var saveRightsBtn = document.getElementById('saveRightsBtn');
    if (saveRightsBtn) {
        saveRightsBtn.addEventListener('click', function () {
            var selected = Array.from(document.querySelectorAll('.rights-cb:checked'))
                               .map(function (cb) { return cb.value; });
            setLoading(saveRightsBtn, true);
            fetch('/api/setrights', {
                method:  'POST',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify({ user_id: parseInt(userId, 10), rights: selected })
            })
            .then(function (r) { return r.ok ? null : Promise.reject(); })
            .then(function ()  { toast('Права сохранены', 'ok'); })
            .catch(function () { toast('Ошибка сохранения прав', 'err'); })
            .finally(function () { setLoading(saveRightsBtn, false); });
        });
    }

    // Auto-load rights on page open if user is already admin
    if (isAdmin) loadRights();

    // ── Grant bonuses / photos ────────────────────────────────────────────────
    bindForm('grantForm', function (e) {
        e.preventDefault();
        var bonus = parseInt(val('bonusInput'), 10) || 0;
        var count = parseInt(val('countInput'), 10) || 0;
        if (bonus === 0 && count === 0) {
            toast('Введите количество бонусов или фото', 'warn');
            return;
        }
        var btn = e.target.querySelector('[type=submit]');
        setLoading(btn, true);

        post('/api/users/edit', { id: userId, avatars: '0', bonus: String(bonus), count: String(count) })
        .then(function (r) { return r.ok ? refreshStats(userId) : Promise.reject(); })
        .then(function () {
            document.getElementById('bonusInput').value = '0';
            document.getElementById('countInput').value = '0';
            toast('Начислено успешно', 'ok');
        })
        .catch(function () { toast('Ошибка при начислении', 'err'); })
        .finally(function () { setLoading(btn, false); });
    });

    // ── Add avatar slot ───────────────────────────────────────────────────────
    bindForm('avatarForm', function (e) {
        e.preventDefault();
        var btn = e.target.querySelector('[type=submit]');
        setLoading(btn, true);

        post('/api/users/edit', { id: userId, avatars: '1', count: '0', bonus: '0' })
        .then(function (r) { return r.ok ? refreshStats(userId) : Promise.reject(); })
        .then(function () { toast('Аватар добавлен', 'ok'); })
        .catch(function () { toast('Ошибка при добавлении аватара', 'err'); })
        .finally(function () { setLoading(btn, false); });
    });

    // ── Toggle admin status ───────────────────────────────────────────────────
    bindForm('statusForm', function (e) {
        e.preventDefault();
        if (!confirm('Изменить роль пользователя?')) return;
        var btn = e.target.querySelector('[type=submit]');
        setLoading(btn, true);

        post('/api/users/changestatus', { id: userId })
        .then(function (r) {
            if (!r.ok) return Promise.reject();
            isAdmin = !isAdmin;
            hero.dataset.isAdmin = String(isAdmin);
            applyAdminState(isAdmin, btn);
            toast(isAdmin ? 'Назначен администратором' : 'Права администратора сняты', 'ok');
        })
        .catch(function () { toast('Ошибка при смене роли', 'err'); })
        .finally(function () { setLoading(btn, false); });
    });

    // ── Delete user ───────────────────────────────────────────────────────────
    bindForm('deleteForm', function (e) {
        e.preventDefault();
        if (!confirm('Удалить пользователя? Это действие необратимо.')) return;
        var btn = e.target.querySelector('[type=submit]');
        setLoading(btn, true);

        post('/api/users/remove', { id: userId })
        .then(function (r) {
            if (r.ok) { window.location.href = '/'; return; }
            return Promise.reject();
        })
        .catch(function () {
            setLoading(btn, false);
            toast('Ошибка при удалении', 'err');
        });
    });

    // ── Helpers ───────────────────────────────────────────────────────────────

    function refreshStats(id) {
        return fetch('/api/users?id=' + id)
            .then(function (r) { return r.json(); })
            .then(function (u) {
                setText('statCount',             u.count             + ' шт.');
                setText('statBonus',             u.bonuses           + ' шт.');
                setText('statAvatars',           u.avatars           + ' шт.');
                setText('statAvatarsAvailable',  u.avatarsAvailable  + ' шт.');
            });
    }

    function applyAdminState(admin, btn) {
        // Hero badge
        var badge = document.getElementById('adminBadge');
        if (badge) badge.style.display = admin ? 'inline-flex' : 'none';

        // Rights block
        var rightsBlock = document.getElementById('rightsBlock');
        if (rightsBlock) {
            rightsBlock.style.display = admin ? '' : 'none';
            if (admin) loadRights(); // loads once (rightsLoaded guard)
        }

        // Status button
        if (btn) {
            btn.className = admin ? 'btn btn-outline-danger' : 'btn btn-outline-primary';
            btn.innerHTML = admin
                ? '<i class="bi bi-shield-x me-1"></i>Убрать из администраторов'
                : '<i class="bi bi-shield-plus me-1"></i>Добавить в администраторы';
        }
    }
}());

// ── Show-user page: balance-changes filter + pagination ──────────────────────
(function () {
    var table = document.getElementById('changesTable');
    if (!table) return;

    var body        = document.getElementById('changesBody');
    var filterGroup = document.getElementById('changesFilter');
    var loadBtn     = document.getElementById('loadMoreChanges');
    var userId      = table.dataset.userId;
    var currentFilter = 'ALL';

    function applyFilter() {
        Array.from(body.querySelectorAll('tr[data-credit-type]')).forEach(function (row) {
            var ct = row.getAttribute('data-credit-type');
            row.style.display = (currentFilter === 'ALL' || ct === currentFilter) ? '' : 'none';
        });
    }

    function fmtDateTime(iso) {
        if (!iso) return '';
        var m = String(iso).match(/(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/);
        if (!m) return esc(iso);
        return m[3] + '.' + m[2] + '.' + m[1] + ' г. ' + m[4] + ':' + m[5];
    }

    function creditEmoji(ct) {
        if (ct === 'PHOTOS')  return ' ⚡️';
        if (ct === 'BONUSES') return ' 🎁';
        return '';
    }

    function changeRow(c) {
        var cls = c.credits < 0 ? 'text-danger' : 'text-success';
        var req = (c.requestType == null || c.requestType === '') ? '-' : esc(c.requestType);
        return '<tr data-credit-type="' + esc(c.creditType) + '">'
            + '<td>' + fmtDateTime(c.dateTime) + '</td>'
            + '<td>' + esc(c.type) + '</td>'
            + '<td>' + req + '</td>'
            + '<td><span class="fw-semibold ' + cls + '">' + esc(c.credits) + creditEmoji(c.creditType) + '</span></td>'
            + '</tr>';
    }

    if (filterGroup) {
        filterGroup.addEventListener('click', function (e) {
            var btn = e.target.closest('button[data-filter]');
            if (!btn) return;
            currentFilter = btn.dataset.filter;
            Array.from(filterGroup.querySelectorAll('button')).forEach(function (b) {
                var active = b === btn;
                b.classList.toggle('btn-primary', active);
                b.classList.toggle('btn-outline-secondary', !active);
            });
            applyFilter();
        });
    }

    if (loadBtn) {
        loadBtn.addEventListener('click', function () {
            var page = parseInt(loadBtn.dataset.nextPage, 10);
            setLoading(loadBtn, true);
            fetch('/api/balance-changes?id=' + userId + '&page=' + page)
                .then(function (r) { return r.ok ? r.json() : Promise.reject(); })
                .then(function (data) {
                    var changes = data.changes || [];
                    var empty = document.getElementById('changesEmpty');
                    if (empty) empty.remove();
                    changes.forEach(function (c) { body.insertAdjacentHTML('beforeend', changeRow(c)); });
                    applyFilter();
                    loadBtn.dataset.nextPage = String(page + 1);
                    if (page >= (data.totalPages || page)) {
                        loadBtn.remove();
                    } else {
                        setLoading(loadBtn, false);
                    }
                })
                .catch(function () {
                    setLoading(loadBtn, false);
                    toast('Ошибка загрузки операций', 'err');
                });
        });
    }
}());

// ── Shared utilities ──────────────────────────────────────────────────────────

function bindForm(id, handler) {
    var f = document.getElementById(id);
    if (f) f.addEventListener('submit', handler);
}

function post(url, data) {
    return fetch(url, { method: 'POST', body: new URLSearchParams(data) });
}

function val(id) {
    var el = document.getElementById(id);
    return el ? el.value : '';
}

function setText(id, text) {
    var el = document.getElementById(id);
    if (el) el.textContent = text;
}

function esc(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function setLoading(btn, loading) {
    if (!btn) return;
    btn.disabled = loading;
    if (loading) {
        btn._origHTML = btn.innerHTML;
        btn.innerHTML = '<span class="btn-spinner"></span>';
    } else if (btn._origHTML) {
        btn.innerHTML = btn._origHTML;
    }
}

function toast(msg, type) {
    var prev = document.querySelector('.app-toast');
    if (prev) prev.remove();

    var t = document.createElement('div');
    t.className = 'app-toast ' + (type || 'info');
    t.textContent = msg;
    document.body.appendChild(t);

    requestAnimationFrame(function () {
        requestAnimationFrame(function () { t.classList.add('show'); });
    });

    setTimeout(function () {
        t.classList.remove('show');
        setTimeout(function () { t.remove(); }, 300);
    }, 3000);
}
