import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import HistoryPage from '@/app/history/page';

// 简单的核心行为测试：验证历史页能渲染
describe('HistoryPage', () => {
  it('renders history title', () => {
    render(<HistoryPage />);
    expect(screen.getByText('个人历史任务')).toBeInTheDocument();
  });
});
