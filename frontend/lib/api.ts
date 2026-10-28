const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

function getUserId() {
  if (typeof window === 'undefined') return 'demo-default';
  let uid = localStorage.getItem('mm_user_id');
  if (!uid) {
    uid = 'demo-' + Math.random().toString(36).slice(2, 10);
    localStorage.setItem('mm_user_id', uid);
  }
  return uid;
}

export async function apiFetch(path: string, options: RequestInit = {}) {
  const headers: Record<string, string> = {
    'X-Demo-User-Id': getUserId(),
    ...(options.headers as Record<string, string> || {}),
  };
  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  return res;
}

export async function getTemplates(category?: string) {
  const q = category ? `?category=${encodeURIComponent(category)}` : '';
  const res = await apiFetch(`/api/templates${q}`);
  const json = await res.json();
  return json.data || [];
}

export async function getTemplate(id: string) {
  const res = await apiFetch(`/api/templates/${id}`);
  const json = await res.json();
  if (!json.success) throw new Error(json.message);
  return json.data;
}

export async function createTask(templateId: string, files: File[]) {
  const form = new FormData();
  form.append('templateId', templateId);
  files.forEach((f) => form.append('files', f));
  const res = await apiFetch('/api/tasks', { method: 'POST', body: form });
  const json = await res.json();
  if (!json.success) throw new Error(json.message);
  return json.data;
}

export async function getTask(taskId: string) {
  const res = await apiFetch(`/api/tasks/${taskId}`);
  const json = await res.json();
  if (!json.success) throw new Error(json.message);
  return json.data;
}

export async function getMyTasks() {
  const res = await apiFetch('/api/tasks');
  const json = await res.json();
  return json.data || [];
}

export async function ensureUser() {
  const res = await apiFetch('/api/users/me');
  const json = await res.json();
  return json.data;
}

export { API_BASE, getUserId };
