import { create } from 'zustand';

export type TaskState = {
  id: string;
  templateId: string;
  status: 'WAITING' | 'PROCESSING' | 'SUCCESS' | 'FAILED';
  stage: 'ASSET_CHECK' | 'GENERATE' | 'FINALIZE' | null;
  failStage?: string | null;
  failReason?: string | null;
  resultUrl?: string | null;
  createdAt?: string;
  updatedAt?: string;
  queryError?: boolean;
};

interface TaskStore {
  tasksMap: Record<string, TaskState>;
  activeTaskIds: string[];
  upsertTask: (task: Partial<TaskState> & { id: string }) => void;
  startTracking: (id: string) => void;
  stopTracking: (id: string) => void;
  setQueryError: (id: string, err: boolean) => void;
  clear: () => void;
}

export const useTaskStore = create<TaskStore>((set) => ({
  tasksMap: {},
  activeTaskIds: [],
  upsertTask: (task) => set((state) => ({
    tasksMap: {
      ...state.tasksMap,
      [task.id]: { ...(state.tasksMap[task.id] || {} as TaskState), ...task, id: task.id },
    }
  })),
  startTracking: (id) => set((state) => {
    if (state.activeTaskIds.includes(id)) return state;
    return { activeTaskIds: [...state.activeTaskIds, id] };
  }),
  stopTracking: (id) => set((state) => ({
    activeTaskIds: state.activeTaskIds.filter((x) => x !== id),
  })),
  setQueryError: (id, err) => set((state) => ({
    tasksMap: {
      ...state.tasksMap,
      [id]: { ...(state.tasksMap[id] || {} as TaskState), id, queryError: err },
    }
  })),
  clear: () => set({ tasksMap: {}, activeTaskIds: [] }),
}));
