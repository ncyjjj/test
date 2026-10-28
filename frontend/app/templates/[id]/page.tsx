'use client';

import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { createTask, getTemplate, API_BASE } from '@/lib/api';
import { useTaskStore } from '@/lib/store';

export default function TemplateDetailPage() {
  const params = useParams();
  const id = params?.id as string;
  const [template, setTemplate] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [files, setFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [submitMsg, setSubmitMsg] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { startTracking, upsertTask } = useTaskStore();

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError('');
    getTemplate(id)
      .then((data) => setTemplate(data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  const required = template?.requiredImageCount || 0;
  const accepted = template?.acceptedMimeTypes ? JSON.parse(template.acceptedMimeTypes) : ['image/jpeg','image/png'];
  const maxBytes = template?.maxFileSizeBytes || 5242880;

  function addFiles(list: FileList | null) {
    if (!list) return;
    const arr = Array.from(list);
    for (const f of arr) {
      if (!accepted.includes(f.type)) { alert(`不支持的类型：${f.type}`); continue; }
      if (f.size > maxBytes) { alert(`文件过大：${f.name}`); continue; }
      setFiles((prev) => [...prev, f].slice(0, required));
    }
  }

  useEffect(() => {
    const urls = files.map((f) => URL.createObjectURL(f));
    setPreviews(urls);
    return () => urls.forEach((u) => URL.revokeObjectURL(u));
  }, [files]);

  function removeAt(idx: number) {
    setFiles((prev) => prev.filter((_, i) => i !== idx));
  }

  async function handleSubmit() {
    if (!template) return;
    if (files.length !== required) { alert(`请上传 ${required} 张图片`); return; }
    setSubmitting(true);
    setSubmitMsg('');
    try {
      const task = await createTask(template.id, files);
      upsertTask({ id: task.id, templateId: task.templateId, status: task.status, stage: task.stage });
      startTracking(task.id);
      setSubmitMsg(`任务已受理（任务ID：${task.id}）正在后台处理...`);
    } catch (e: any) {
      setSubmitMsg(`提交失败：${e.message}`);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="max-w-4xl mx-auto px-4 py-8">
      <header className="mb-6">
        <Link href="/" className="text-sm text-cyan-400">← 返回模板列表</Link>
        <h1 className="text-2xl md:text-3xl font-semibold mt-2">{template?.title || '模板详情'}</h1>
        <p className="text-slate-400 mt-1">{template?.description}</p>
      </header>

      {loading && <div className="text-slate-400">加载中...</div>}
      {error && <div className="text-red-400">加载失败：{error}</div>}

      {template && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <section className="rounded-lg overflow-hidden bg-slate-900/60 border border-slate-800">
            <img src={template.coverUrl} alt={template.title} className="w-full h-full object-cover" />
          </section>
          <section className="rounded-lg bg-slate-900/60 border border-slate-800 p-5">
            <h2 className="text-lg font-medium">素材上传与提交</h2>
            <p className="text-sm text-slate-400 mt-1">需要上传 {required} 张图片，支持 {accepted.join(' / ')}，单文件 ≤ {Math.round(maxBytes/1024/1024)}MB</p>

            <div className="mt-4 grid grid-cols-2 gap-3">
              {Array.from({ length: required }).map((_, idx) => (
                <div key={idx} className="relative aspect-[4/3] rounded bg-slate-800 overflow-hidden flex items-center justify-center">
                  {previews[idx] ? (
                    <>
                      <img src={previews[idx]} alt={`预览${idx+1}`} className="w-full h-full object-cover" />
                      <button onClick={() => removeAt(idx)} className="absolute top-1 right-1 text-xs px-2 py-0.5 rounded bg-black/60">移除</button>
                    </>
                  ) : (
                    <button onClick={() => fileInputRef.current?.click()} className="text-sm text-slate-400" aria-label={`上传图片位 ${idx+1}`}>选择图片</button>
                  )}
                </div>
              ))}
            </div>
            <input ref={fileInputRef} type="file" accept={accepted.join(',')} multiple onChange={(e) => addFiles(e.target.files)} className="hidden" />

            <div className="mt-5 flex items-center gap-3">
              <button
                onClick={handleSubmit}
                disabled={submitting || files.length !== required}
                className="px-4 py-2 rounded bg-cyan-500 text-black disabled:opacity-50"
              >
                {submitting ? '提交中...' : '提交制作任务'}
              </button>
              <Link href="/history" className="text-sm text-slate-300">查看历史记录</Link>
            </div>
            {submitMsg && <p className="mt-3 text-sm text-slate-300">{submitMsg}</p>}
            <p className="mt-4 text-xs text-slate-500">提示：提交接口在创建任务后即刻返回任务 ID，后台按顺序执行（素材检查 → 模拟生成 → 结果整理），关闭页面不影响后台执行。</p>
          </section>
        </div>
      )}
    </main>
  );
}
