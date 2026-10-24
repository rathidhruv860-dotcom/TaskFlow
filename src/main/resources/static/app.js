(() => {
  const $ = (selector) => document.querySelector(selector);
  const state = {
    token: localStorage.getItem('taskflow.token') || '',
    user: JSON.parse(localStorage.getItem('taskflow.user') || 'null'),
    tasks: [],
    users: [],
    registerMode: false,
    editingTask: null,
    toastTimer: null
  };

  const authScreen = $('#authScreen');
  const appScreen = $('#appScreen');
  const authForm = $('#authForm');
  const taskForm = $('#taskForm');

  function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (char) => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);
  }

  function errorMessage(data, fallback) {
    if (!data) return fallback;
    if (data.fields) return Object.values(data.fields).join(' ');
    return data.error || data.message || fallback;
  }

  async function api(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (state.token) headers.set('Authorization', `Bearer ${state.token}`);
    if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
    const response = await fetch(path, { ...options, headers });
    if (response.status === 401 && state.token) {
      signOut(false);
      throw new Error('Your session expired. Please sign in again.');
    }
    if (response.status === 204) return null;
    const data = await response.json().catch(() => null);
    if (!response.ok) throw new Error(errorMessage(data, `Request failed (${response.status}).`));
    return data;
  }

  function showAuth() {
    authScreen.classList.remove('hidden');
    appScreen.classList.add('hidden');
  }

  function showApp() {
    authScreen.classList.add('hidden');
    appScreen.classList.remove('hidden');
    $('#profileName').textContent = state.user?.name || 'TaskFlow user';
    $('#profileEmail').textContent = state.user?.email || '';
    $('#avatar').textContent = (state.user?.name || 'T').trim().charAt(0).toUpperCase();
    refreshData();
  }

  function signOut(showMessage = true) {
    state.token = '';
    state.user = null;
    state.tasks = [];
    state.users = [];
    localStorage.removeItem('taskflow.token');
    localStorage.removeItem('taskflow.user');
    showAuth();
    if (showMessage) setAuthError('');
  }

  function setAuthError(message) { $('#authError').textContent = message || ''; }

  function setRegisterMode(enabled) {
    state.registerMode = enabled;
    $('#nameField').classList.toggle('hidden', !enabled);
    $('#authEyebrow').textContent = enabled ? 'CREATE YOUR WORKSPACE' : 'WELCOME BACK';
    $('#authTitle').textContent = enabled ? 'Create your account' : 'Sign in to your workspace';
    $('#authSubtitle').textContent = enabled ? 'Start organizing your work today.' : 'Pick up where you left off.';
    $('#authSubmit').innerHTML = enabled ? 'Create account <span>→</span>' : 'Sign in <span>→</span>';
    $('#authSwitchText').textContent = enabled ? 'Already have an account?' : 'New to TaskFlow?';
    $('#authSwitch').textContent = enabled ? 'Sign in' : 'Create an account';
    $('#authPassword').setAttribute('autocomplete', enabled ? 'new-password' : 'current-password');
    setAuthError('');
  }

  async function refreshData() {
    $('#loadingState').classList.remove('hidden');
    $('#taskList').innerHTML = '';
    $('#pageError').classList.add('hidden');
    try {
      const [tasks, users] = await Promise.all([
        api(buildTaskUrl()),
        api('/api/users')
      ]);
      state.tasks = tasks || [];
      state.users = users || [];
      render();
    } catch (error) {
      $('#pageError').textContent = error.message;
      $('#pageError').classList.remove('hidden');
    } finally {
      $('#loadingState').classList.add('hidden');
    }
  }

  function buildTaskUrl() {
    const params = new URLSearchParams();
    if ($('#statusFilter')?.value) params.set('status', $('#statusFilter').value);
    if ($('#priorityFilter')?.value) params.set('priority', $('#priorityFilter').value);
    if ($('#dueAfterFilter')?.value) params.set('dueAfter', $('#dueAfterFilter').value);
    if ($('#dueBeforeFilter')?.value) params.set('dueBefore', $('#dueBeforeFilter').value);
    const query = params.toString();
    return `/api/tasks${query ? `?${query}` : ''}`;
  }

  function render() {
    const all = state.tasks;
    const query = $('#searchInput').value.trim().toLowerCase();
    const visible = query ? all.filter((task) =>
      `${task.title} ${task.description || ''} ${task.assigneeName || ''}`.toLowerCase().includes(query)
    ) : all;
    $('#statAll').textContent = all.length;
    $('#statTodo').textContent = all.filter((task) => task.status === 'TODO').length;
    $('#statProgress').textContent = all.filter((task) => task.status === 'IN_PROGRESS').length;
    $('#statDone').textContent = all.filter((task) => task.status === 'DONE').length;
    $('#navTaskCount').textContent = all.length;
    $('#visibleCount').textContent = `${visible.length} ${visible.length === 1 ? 'task' : 'tasks'}`;
    $('#taskList').innerHTML = visible.map(taskCard).join('');
    const noResults = visible.length === 0;
    $('#emptyState').classList.toggle('hidden', !noResults);
    $('#emptyTitle').textContent = all.length === 0 ? 'Your task list is clear' : 'No tasks match these filters';
    $('#emptyMessage').textContent = all.length === 0
      ? 'Add a task to get your plans moving.'
      : 'Try changing the search or filters.';
    $('#emptyCreateButton').classList.toggle('hidden', all.length > 0);
  }

  function taskCard(task) {
    const isOwner = Number(task.ownerId) === Number(state.user?.id);
    const dueDate = task.dueDate ? new Date(`${task.dueDate}T00:00:00`) : null;
    const overdue = dueDate && dueDate < new Date(new Date().toDateString()) && task.status !== 'DONE';
    const statusLabel = { TODO: 'To do', IN_PROGRESS: 'In progress', DONE: 'Done' }[task.status] || task.status;
    const priorityLabel = { HIGH: 'High', MEDIUM: 'Medium', LOW: 'Low' }[task.priority] || task.priority;
    const dueLabel = dueDate ? dueDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' }) : 'No due date';
    const initial = (task.assigneeName || 'U').trim().charAt(0).toUpperCase();
    return `<article class="task-row">
      <div class="task-main"><div class="task-title-line"><h3 class="task-title" title="${escapeHtml(task.title)}">${escapeHtml(task.title)}</h3></div><p class="task-description">${escapeHtml(task.description || 'No description')}</p></div>
      <div class="status-column"><select class="status-pill status-${escapeHtml(String(task.status).toLowerCase().replace('_','-'))}" data-action="status" data-id="${task.id}" aria-label="Change task status">${statusOptions(task.status)}</select></div>
      <div class="priority-column"><span class="priority-pill priority-${escapeHtml(String(task.priority).toLowerCase())}">${escapeHtml(priorityLabel)}</span></div>
      <div class="due-date ${overdue ? 'due-overdue' : ''}"><span>Due date</span><strong>${escapeHtml(dueLabel)}${overdue ? ' · overdue' : ''}</strong></div>
      <div class="assignee-cell"><span class="mini-avatar">${escapeHtml(initial)}</span><span>${escapeHtml(task.assigneeName || (isOwner ? 'Unassigned' : 'Assigned to you'))}</span></div>
      <div class="task-actions">${isOwner ? `<button class="icon-button" data-action="edit" data-id="${task.id}" aria-label="Edit task" title="Edit">✎</button><button class="icon-button" data-action="delete" data-id="${task.id}" aria-label="Delete task" title="Delete">⌫</button>` : ''}</div>
    </article>`;
  }

  function statusOptions(selected) {
    return [['TODO','To do'],['IN_PROGRESS','In progress'],['DONE','Done']]
      .map(([value,label]) => `<option value="${value}" ${selected === value ? 'selected' : ''}>${label}</option>`).join('');
  }

  function showToast(message, isError = false) {
    const toast = $('#toast');
    toast.textContent = message;
    toast.classList.toggle('toast-error', isError);
    toast.classList.add('show');
    clearTimeout(state.toastTimer);
    state.toastTimer = setTimeout(() => toast.classList.remove('show'), 2800);
  }

  function openModal(task = null) {
    state.editingTask = task;
    $('#modalTitle').textContent = task ? 'Edit task' : 'Create a task';
    $('#saveTaskButton').textContent = task ? 'Save changes' : 'Create task';
    $('#taskTitle').value = task?.title || '';
    $('#taskDescription').value = task?.description || '';
    $('#taskStatus').value = task?.status || 'TODO';
    $('#taskPriority').value = task?.priority || 'MEDIUM';
    $('#taskDueDate').value = task?.dueDate || '';
    const assigneeSelect = $('#taskAssignee');
    assigneeSelect.innerHTML = '<option value="">Unassigned</option>' + state.users.map((user) =>
      `<option value="${user.id}">${escapeHtml(user.name)} · ${escapeHtml(user.email)}</option>`).join('');
    assigneeSelect.value = task?.assigneeId ? String(task.assigneeId) : '';
    $('#modalError').textContent = '';
    $('#taskModal').classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    $('#taskTitle').focus();
  }

  function closeModal() {
    $('#taskModal').classList.add('hidden');
    document.body.style.overflow = '';
    state.editingTask = null;
  }

  async function saveTask(event) {
    event.preventDefault();
    const task = state.editingTask;
    const payload = {
      title: $('#taskTitle').value.trim(),
      description: $('#taskDescription').value.trim() || null,
      status: $('#taskStatus').value,
      priority: $('#taskPriority').value,
      dueDate: $('#taskDueDate').value || null
    };
    const selectedAssignee = $('#taskAssignee').value;
    const button = $('#saveTaskButton');
    button.disabled = true;
    button.textContent = 'Saving…';
    $('#modalError').textContent = '';
    try {
      let saved;
      if (task) {
        saved = await api(`/api/tasks/${task.id}`, { method: 'PUT', body: JSON.stringify(payload) });
      } else {
        saved = await api('/api/tasks', { method: 'POST', body: JSON.stringify(payload) });
      }
      if (selectedAssignee) {
        await api(`/api/tasks/${saved.id}/assignee`, {
          method: 'PATCH', body: JSON.stringify({ assigneeId: Number(selectedAssignee) })
        });
      } else if (task?.assigneeId) {
        await api(`/api/tasks/${saved.id}/assignee`, { method: 'DELETE' });
      }
      closeModal();
      await refreshData();
      showToast(task ? 'Task updated.' : 'Task created.');
    } catch (error) {
      $('#modalError').textContent = error.message;
    } finally {
      button.disabled = false;
      button.textContent = state.editingTask ? 'Save changes' : 'Create task';
    }
  }

  async function handleTaskAction(event) {
    const button = event.target.closest('[data-action]');
    if (!button) return;
    const id = Number(button.dataset.id);
    const action = button.dataset.action;
    const task = state.tasks.find((item) => Number(item.id) === id);
    if (!task) return;
    try {
      if (action === 'edit') openModal(task);
      if (action === 'delete' && window.confirm(`Delete “${task.title}”? This cannot be undone.`)) {
        await api(`/api/tasks/${id}`, { method: 'DELETE' });
        await refreshData();
        showToast('Task deleted.');
      }
    } catch (error) { showToast(error.message, true); }
  }

  async function changeStatus(event) {
    const select = event.target.closest('select[data-action="status"]');
    if (!select) return;
    const id = Number(select.dataset.id);
    select.disabled = true;
    try {
      await api(`/api/tasks/${id}/status`, {
        method: 'PATCH', body: JSON.stringify({ status: select.value })
      });
      await refreshData();
      showToast('Status updated.');
    } catch (error) {
      showToast(error.message, true);
      await refreshData();
    }
  }

  function debounce(callback, delay = 220) {
    let timer;
    return (...args) => { clearTimeout(timer); timer = setTimeout(() => callback(...args), delay); };
  }

  authForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    setAuthError('');
    const payload = {
      email: $('#authEmail').value.trim(),
      password: $('#authPassword').value
    };
    if (state.registerMode) payload.name = $('#authName').value.trim();
    const submit = $('#authSubmit');
    submit.disabled = true;
    submit.textContent = state.registerMode ? 'Creating account…' : 'Signing in…';
    try {
      const result = await api(state.registerMode ? '/api/auth/register' : '/api/auth/login', {
        method: 'POST', body: JSON.stringify(payload)
      });
      state.token = result.token;
      state.user = result.user;
      localStorage.setItem('taskflow.token', state.token);
      localStorage.setItem('taskflow.user', JSON.stringify(state.user));
      authForm.reset();
      setRegisterMode(false);
      showApp();
    } catch (error) {
      setAuthError(error.message);
    } finally {
      submit.disabled = false;
      submit.innerHTML = state.registerMode ? 'Create account <span>→</span>' : 'Sign in <span>→</span>';
    }
  });

  $('#authSwitch').addEventListener('click', () => setRegisterMode(!state.registerMode));
  $('#logoutButton').addEventListener('click', () => signOut());
  $('#newTaskButton').addEventListener('click', () => openModal());
  $('#emptyCreateButton').addEventListener('click', () => openModal());
  $('#closeModal').addEventListener('click', closeModal);
  $('#cancelModal').addEventListener('click', closeModal);
  $('#taskModal').addEventListener('click', (event) => { if (event.target === $('#taskModal')) closeModal(); });
  document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && !$('#taskModal').classList.contains('hidden')) closeModal(); });
  taskForm.addEventListener('submit', saveTask);
  $('#taskList').addEventListener('click', handleTaskAction);
  $('#taskList').addEventListener('change', changeStatus);
  ['statusFilter','priorityFilter','dueAfterFilter','dueBeforeFilter'].forEach((id) => {
    $(`#${id}`).addEventListener('change', refreshData);
  });
  $('#searchInput').addEventListener('input', debounce(render));
  $('#clearFilters').addEventListener('click', () => {
    $('#statusFilter').value = '';
    $('#priorityFilter').value = '';
    $('#dueAfterFilter').value = '';
    $('#dueBeforeFilter').value = '';
    $('#searchInput').value = '';
    refreshData();
  });

  if (state.token && state.user) showApp();
  else showAuth();
})();
