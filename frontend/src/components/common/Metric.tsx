import React from 'react';

interface MetricProps {
  label: string;
  value: string | number;
  subtext?: string;
  badge?: React.ReactNode;
}

export const Metric: React.FC<MetricProps> = ({ label, value, subtext, badge }) => {
  return (
    <div className="metric-card">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span className="metric-label">{label}</span>
        {badge}
      </div>
      <div className="metric-value">{value}</div>
      {subtext && <div className="metric-sub">{subtext}</div>}
    </div>
  );
};
