'use client';

import { useEffect, useRef } from 'react';
import Link from 'next/link';
import { getMyTasks, getTask } from '@/lib/api';
import { useTaskStore } from '@/lib/store';

export default function HistoryPage() {
  const { tasksMap, upsertTask, startTracking, stopTracking, setQueryError } = useTaskStore();
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    // 加载历史并恢复跟踪未结束任务
    getMyTasks().then((list) => {
      list.forEach((t: any) => {
        upsertTask(t);
        if (t.status === 'WAITING' || t.status === 'PROCESSING') {
          startTracking(t.id);
        }
      });
    });

    const poll = async () => {
      const active = useTaskStore.getState().activeTaskIds;
      for (const id of active) {
        try {
          const t = await getTask(id);
          useTaskStore.getState().upsertTask(t);
          useTaskStore.getState().setQueryError(id, false);
          if (t.status === 'SUCCESS' || t.status === 'FAILED') {
            useTaskStore.getState().stopTracking(id);
          }
        } catch (e) {
          // 查询异常：不标记任务失败，保留状态
          useTaskStore.getState().setQueryError(id, true);
        }
      }
      const stillActive = useTaskStore.getState().activeTaskIds.length;
      if (stillActive === 0 && timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
    };

    timerRef.current = setInterval(poll, 1200);
    poll();

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  const tasks = Object.values(tasksMap).sort((a: any, b: any) => (b.createdAt || '').localeCompare(a.createdAt || ''));

  function statusText(s: string) {
    if (s === 'WAITING') return '等待处理';
    if (s === 'PROCESSING') return '处理中';
    if (s === 'SUCCESS') return '成功';
    if (s === 'FAILED') return '失败';
    return s;
  }

  function stageText(s: string | null) {
    if (s === 'ASSET_CHECK') return '素材检查';
    if (s === 'GENERATE') return '模拟生成';
    if (s === 'FINALIZE') return '结果整理';
    return '-';
  }

  return (
    <main className="max-w-6xl mx-auto px-4 py-8">
      <header className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl md:text-3xl font-semibold">个人历史任务</h1>
          <p className="text-slate-400 mt-1">自动跟踪未完成任务，刷新页面后可恢复记录</p>
        </div>
        <Link href="/" className="text-sm text-cyan-400">← 返回模板列表</Link>
      </header>

      {tasks.length === 0 && <div className="text-slate-400">暂无任务记录</div>}

      <div className="space-y-3">
        {tasks.map((t: any) => (
          <article key={t.id} className="rounded-lg bg-slate-900/60 border border-slate-800 p-4">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-2">
              <div>
                <div className="text-sm text-slate-400">任务ID：{t.id}</div>
                <div className="mt-1">模板：{t.templateId} · 状态：<span className={t.status === 'SUCCESS' ? 'text-green-400' : t.status === 'FAILED' ? 'text-red-400' : 'text-cyan-400'}>{statusText(t.status)}</span> · 当前阶段：{stageText(t.stage)}</div>
              </div>
              <div className="text-xs text-slate-500">创建：{t.createdAt || '-'} 更新：{t.updatedAt || '-'}</div>
            </div>
            {t.queryError && <div className="mt-2 text-xs text-yellow-400">查询暂时不可用，已保留当前任务信息，将自动恢复查询</div>}
            {t.status === 'FAILED' && <div className="mt-2 text-sm text-red-300">失败原因：{t.failReason || '-' }（失败阶段：{t.failStage || '-'})</div>}
            {t.status === 'SUCCESS' && t.resultUrl && (
              <div className="mt-3">
                <div className="text-sm text-green-300 mb-2">制作成功，可预览结果</div>
                <div className="rounded overflow-hidden border border-slate-800 max-w-md">
                  <img src={t.resultUrl.startsWith('http') ? t.resultUrl : `http://localhost:8080${t.resultUrl}`} alt="制作结果" className="w-full" />
                </div>
              </div>
            )}
          </article>
        ))}
      </div>
    </main>
  );
}
