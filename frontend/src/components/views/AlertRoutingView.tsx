import React, { useState } from 'react';
import { ALERTS } from '../../data/sampleData';
import type { AlertItem } from '../../types/engine';
import { SectionHeader } from '../common/SectionHeader';
import { StatusBadge } from '../common/StatusBadge';
import { Filter, ChevronDown, ChevronRight } from 'lucide-react';

export const AlertRoutingView: React.FC = () => {
  const [selectedSeverity, setSelectedSeverity] = useState<string>('ALL');
  const [expandedAlertId, setExpandedAlertId] = useState<string | null>(null);

  const severities = ['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];

  const filteredAlerts = ALERTS.filter((alert) => {
    if (selectedSeverity === 'ALL') return true;
    return alert.severity === selectedSeverity;
  });

  const toggleExpand = (id: string) => {
    setExpandedAlertId(expandedAlertId === id ? null : id);
  };

  return (
    <div>
      <SectionHeader
        tag="STAGE 07 — PRIORITY SCHEDULING"
        title="Alert Prioritization & Routing"
        description="Prioritized alert scheduling queue structured to order incoming threat signals using a custom Binary Heap without relying on java.util.PriorityQueue."
        actionRow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <StatusBadge type="simulated" label="ROUTING SIMULATION" />
          </div>
        }
      />

      {/* Filter Row */}
      <div className="filter-bar" style={{ padding: '12px 16px', backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
            <Filter size={12} /> Severity:
          </span>
          {severities.map((sev) => (
            <button
              key={sev}
              className={`btn-action ${selectedSeverity === sev ? 'active' : ''}`}
              onClick={() => setSelectedSeverity(sev)}
              style={{ padding: '3px 10px', fontSize: '11px', fontFamily: 'var(--font-mono)' }}
            >
              {sev}
            </button>
          ))}
        </div>

        <span className="mono-text" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
          Queue depth: {filteredAlerts.length} alerts
        </span>
      </div>

      {/* Alerts List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '16px' }}>
        {filteredAlerts.map((alert: AlertItem) => {
          const isExpanded = expandedAlertId === alert.id;
          return (
            <div
              key={alert.id}
              style={{
                backgroundColor: 'var(--surface)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                overflow: 'hidden',
                transition: 'border-color var(--transition-fast)'
              }}
            >
              {/* Alert Header Row */}
              <div
                onClick={() => toggleExpand(alert.id)}
                data-clickable="true"
                style={{
                  padding: '14px 18px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  cursor: 'pointer',
                  flexWrap: 'wrap',
                  gap: '12px'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                  {isExpanded ? (
                    <ChevronDown size={14} color="var(--text-muted)" />
                  ) : (
                    <ChevronRight size={14} color="var(--text-muted)" />
                  )}

                  <StatusBadge
                    type={alert.severity.toLowerCase() as any}
                    label={alert.severity}
                  />

                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '2px' }}>
                      <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
                        {alert.title}
                      </span>
                    </div>
                    <div className="mono-text" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                      {alert.timestamp} · {alert.hostId} · {alert.stage}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <span className="mono-text" style={{ fontSize: '11px', color: 'var(--text-subtle)' }}>
                    {alert.id}
                  </span>
                  <span
                    className="tech-label"
                    style={{
                      padding: '2px 6px',
                      backgroundColor: 'var(--surface-elevated)',
                      border: '1px solid var(--border)',
                      borderRadius: 'var(--radius-xs)',
                      color: alert.status === 'MITIGATED' ? 'var(--status-verified)' : 'var(--text-secondary)'
                    }}
                  >
                    {alert.status}
                  </span>
                </div>
              </div>

              {/* Expanded Alert Details */}
              {isExpanded && (
                <div style={{
                  padding: '16px 20px',
                  backgroundColor: 'var(--surface-elevated)',
                  borderTop: '1px solid var(--border)',
                  fontSize: '12px'
                }}>
                  <div style={{ marginBottom: '10px' }}>
                    <span className="mono-text" style={{ color: 'var(--text-muted)', display: 'block', marginBottom: '2px' }}>
                      EVENT SUMMARY:
                    </span>
                    <p style={{ color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {alert.description}
                    </p>
                  </div>

                  <div style={{
                    padding: '10px 14px',
                    backgroundColor: 'var(--bg)',
                    borderRadius: 'var(--radius-xs)',
                    border: '1px solid var(--border)'
                  }}>
                    <span className="mono-text" style={{ color: 'var(--accent)', display: 'block', marginBottom: '2px', fontSize: '11px' }}>
                      RECOMMENDED AUTOMATED MITIGATION:
                    </span>
                    <span className="mono-text" style={{ color: 'var(--text-primary)', fontSize: '12px' }}>
                      {alert.mitigation}
                    </span>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      <div style={{ marginTop: '20px', padding: '12px 16px', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--surface)' }}>
        <p style={{ fontSize: '11px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
          [NOTICE] Alerts above are generated from simulated kill-chain progression data. The custom Binary Heap priority scheduler is planned for Phase 7 of the Java backend.
        </p>
      </div>
    </div>
  );
};
