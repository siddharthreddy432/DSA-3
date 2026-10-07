import React from 'react';

interface SectionHeaderProps {
  tag: string;
  title: string;
  description?: string;
  actionRow?: React.ReactNode;
}

export const SectionHeader: React.FC<SectionHeaderProps> = ({
  tag,
  title,
  description,
  actionRow
}) => {
  return (
    <header className="section-header">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '8px' }}>
        <span className="section-tag">{tag}</span>
        {actionRow}
      </div>
      <h1 className="section-title">{title}</h1>
      {description && <p className="section-description">{description}</p>}
    </header>
  );
};
