import React from 'react';

interface EmptyStateProps {
  title: string;
  message: string;
  actionText?: string;
  onAction?: () => void;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  message,
  actionText,
  onAction
}) => {
  return (
    <div style={{
      padding: '48px 24px',
      textAlign: 'center',
      border: '1px dashed var(--border-strong)',
      borderRadius: 'var(--radius-sm)',
      backgroundColor: 'var(--surface)'
    }}>
      <h3 style={{ fontSize: '15px', color: 'var(--text-primary)', marginBottom: '8px' }}>
        {title}
      </h3>
      <p style={{ fontSize: '13px', color: 'var(--text-muted)', maxWidth: '420px', margin: '0 auto 16px' }}>
        {message}
      </p>
      {actionText && onAction && (
        <button className="btn-action" onClick={onAction}>
          {actionText}
        </button>
      )}
    </div>
  );
};
