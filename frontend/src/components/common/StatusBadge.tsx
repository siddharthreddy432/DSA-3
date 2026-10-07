import React from 'react';

interface StatusBadgeProps {
  type: 'verified' | 'simulated' | 'planned' | 'demo' | 'critical' | 'high' | 'medium' | 'low';
  label?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ type, label }) => {
  const defaultLabels: Record<string, string> = {
    verified: 'JAVA 17 VERIFIED',
    simulated: 'SIMULATION',
    planned: 'PLANNED',
    demo: 'DEMO DATA',
    critical: 'CRITICAL',
    high: 'HIGH',
    medium: 'MEDIUM',
    low: 'LOW'
  };

  const text = label || defaultLabels[type] || type.toUpperCase();

  return (
    <span className={`badge badge-${type}`}>
      {text}
    </span>
  );
};
