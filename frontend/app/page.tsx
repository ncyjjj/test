'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { getTemplates } from '@/lib/api';

export default function HomePage() {
  const [templates, setTemplates] = useState<any[]>([]);
  const [category, setCategory] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    setError('');
    getTemplates(category || undefined)
      .then((data) => setTemplates(data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [category]);

  const categories = ['', ...Array.from(new Set(templates.map((t) => t.category)))];

  return (
    <main className="max-w-6xl mx-auto px-4 py-8">
      <header className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl md:text-3xl font-semibold">素材制作网站 · 模板列表</h1>
          <p className="text-slate-400 mt-1">浏览模板、上传素材并提交制作任务，后台异步模拟处理</p>
        </div>
        <nav className="flex gap-3 text-sm">
          <Link href="/history" className="px-3 py-2 rounded bg-slate-800 hover:bg-slate-700">历史记录</Link>
        </nav>
      </header>

      <div className="flex flex-wrap gap-2 mb-6">
        {['全部', '创意图片', '趣味合成'].map((c, i) => {
          const val = i === 0 ? '' : c;
          const active = category === val;
          return (
            <button
              key={c}
              onClick={() => setCategory(val)}
              className={`px-3 py-1.5 rounded-full text-sm ${active ? 'bg-cyan-500 text-black' : 'bg-slate-800 hover:bg-slate-700'}`}
            >
              {c}
            </button>
          );
        })}
      </div>

      {loading && <div className="text-slate-400">加载中...</div>}
      {error && <div className="text-red-400">请求失败：{error}</div>}
      {!loading && !error && templates.length === 0 && <div className="text-slate-400">暂无模板</div>}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {templates.map((t) => (
          <Link key={t.id} href={`/templates/${t.id}`} className="group block rounded-lg overflow-hidden bg-slate-900/60 border border-slate-800 hover:border-cyan-400/60 transition">
            <div className="aspect-[16/10] bg-slate-800">
              <img src={t.coverUrl} alt={t.title} className="w-full h-full object-cover" />
            </div>
            <div className="p-4">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-medium">{t.title}</h2>
                <span className="text-xs px-2 py-0.5 rounded bg-slate-800">{t.category}</span>
              </div>
              <p className="text-sm text-slate-400 mt-2 line-clamp-2">{t.description}</p>
              <p className="text-xs text-slate-500 mt-3">所需素材：{t.requiredImageCount} 张（{Array.isArray(JSON.parse(t.acceptedMimeTypes || '[]')) ? JSON.parse(t.acceptedMimeTypes).join(', ') : t.acceptedMimeTypes}，≤{Math.round((t.maxFileSizeBytes||0)/1024/1024)}MB）</p>
            </div>
          </Link>
        ))}
      </div>
    </main>
  );
}
