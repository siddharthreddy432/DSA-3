import React from 'react';
import type { ViewMode } from '../../types/engine';
import {
  Activity,
  Terminal,
  ShieldAlert,
  GitCommit,
  Network,
  Filter,
  Eye,
  Bell
} from 'lucide-react';

interface NavigationProps {
  currentView: ViewMode;
  onSelectView: (view: ViewMode) => void;
}

interface NavEntry {
  id: ViewMode;
  label: string;
  icon: React.ReactNode;
}

export const Navigation: React.FC<NavigationProps> = ({ currentView, onSelectView }) => {
  const navEntries: NavEntry[] = [
    { id: 'overview', label: 'Overview', icon: <Activity className="nav-item-icon" /> },
    { id: 'ingestion', label: 'Log Ingestion', icon: <Terminal className="nav-item-icon" /> },
    { id: 'signatures', label: 'Signature Matching', icon: <ShieldAlert className="nav-item-icon" /> },
    { id: 'killchain', label: 'Kill Chain', icon: <GitCommit className="nav-item-icon" /> },
    { id: 'graph', label: 'Attack Graph', icon: <Network className="nav-item-icon" /> },
    { id: 'chokepoints', label: 'Choke Points', icon: <Filter className="nav-item-icon" /> },
    { id: 'monitoring', label: 'Monitoring', icon: <Eye className="nav-item-icon" /> },
    { id: 'alerts', label: 'Alert Routing', icon: <Bell className="nav-item-icon" /> }
  ];

  return (
    <aside className="app-sidebar">
      <div className="sidebar-header">
        <div className="system-kicker">THREAT ENGINE</div>
        <div className="system-title">Kill-Chain Correlation</div>
      </div>

      <nav className="sidebar-nav" aria-label="Main Navigation">
        {navEntries.map((item) => {
          const isActive = currentView === item.id;
          return (
            <button
              key={item.id}
              className={`nav-item ${isActive ? 'active' : ''}`}
              onClick={() => onSelectView(item.id)}
              aria-current={isActive ? 'page' : undefined}
            >
              <span style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                {item.icon}
                <span>{item.label}</span>
              </span>
              {isActive && <span className="nav-active-dot" />}
            </button>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="engine-status-card">
          <div className="status-row">
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <span className="status-indicator-dot" />
              <span className="status-indicator-text">ONLINE</span>
            </div>
            <span className="engine-phase-label">PHASE 2</span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: '1.4', marginTop: '4px' }}>
            Core DS Verified<br />
            Phase 3+ Simulation Mode
          </div>
        </div>
      </div>
    </aside>
  );
};
