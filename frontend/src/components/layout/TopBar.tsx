import React from 'react';
import type { ViewMode } from '../../types/engine';
import { StatusBadge } from '../common/StatusBadge';
import { ShieldAlert, Terminal, Activity, GitCommit, Network, Filter, Eye, Bell } from 'lucide-react';

interface TopBarProps {
  currentView: ViewMode;
  onSelectView: (view: ViewMode) => void;
}

export const TopBar: React.FC<TopBarProps> = ({ currentView }) => {
  const viewTitles: Record<ViewMode, { label: string; icon: React.ReactNode }> = {
    overview: { label: 'System Overview', icon: <Activity size={14} /> },
    ingestion: { label: 'Log Ingestion', icon: <Terminal size={14} /> },
    signatures: { label: 'Signature Matching', icon: <ShieldAlert size={14} /> },
    killchain: { label: 'Correlation Kill Chain', icon: <GitCommit size={14} /> },
    graph: { label: 'Attack Graph', icon: <Network size={14} /> },
    chokepoints: { label: 'Choke-Point Analysis', icon: <Filter size={14} /> },
    monitoring: { label: 'Monitoring Placement', icon: <Eye size={14} /> },
    alerts: { label: 'Alert Routing', icon: <Bell size={14} /> }
  };

  const active = viewTitles[currentView];

  return (
    <header className="top-bar">
      <div className="top-bar-left">
        <div className="view-breadcrumb">
          <span style={{ color: 'var(--text-muted)' }}>ENGINE</span>
          <span style={{ color: 'var(--text-subtle)' }}>/</span>
          <span className="view-breadcrumb-active" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
            {active.icon}
            {active.label}
          </span>
        </div>
      </div>

      <div className="top-bar-right">
        <StatusBadge type="verified" label="JAVA 17 CORE: PHASE 2 VERIFIED" />
      </div>
    </header>
  );
};
