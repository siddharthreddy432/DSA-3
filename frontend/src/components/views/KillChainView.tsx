import React from 'react';
import { KILL_CHAIN_STAGES, DEMO_SCENARIO_NAME } from '../../data/sampleData';
import { SectionHeader } from '../common/SectionHeader';
import { StatusBadge } from '../common/StatusBadge';


export const KillChainView: React.FC = () => {
  return (
    <div>
      <SectionHeader
        tag="STAGE 03 — ATTRIBUTION MODEL"
        title="Correlation Kill Chain"
        description="Chronological event reconstruction tracking the adversary's linear progression across 5 defined stages from external reconnaissance to data staging and exfiltration."
        actionRow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <StatusBadge type="simulated" label="CORRELATION SIMULATION" />
          </div>
        }
      />

      {/* Scenario Indicator */}
      <div style={{
        padding: '12px 16px',
        backgroundColor: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius-sm)',
        marginBottom: '28px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '8px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span className="tech-label" style={{ color: 'var(--text-muted)' }}>SCENARIO:</span>
          <span className="mono-text" style={{ fontSize: '12px', color: 'var(--text-primary)' }}>{DEMO_SCENARIO_NAME}</span>
        </div>
        <span className="tech-label" style={{ color: 'var(--accent)' }}>5 STAGES CORRELATED</span>
      </div>

      {/* Vertical Timeline Progression */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0', position: 'relative' }}>
        {KILL_CHAIN_STAGES.map((stage, idx) => {
          const isLast = idx === KILL_CHAIN_STAGES.length - 1;
          return (
            <div key={stage.id} style={{ display: 'flex', gap: '20px', position: 'relative' }}>
              {/* Timeline Indicator Column */}
              <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                width: '32px',
                flexShrink: 0
              }}>
                <div style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--surface-elevated)',
                  border: '1px solid var(--accent)',
                  color: 'var(--accent)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '11px',
                  fontFamily: 'var(--font-mono)',
                  fontWeight: 600,
                  zIndex: 2
                }}>
                  {stage.stageNumber}
                </div>

                {!isLast && (
                  <div style={{
                    width: '1px',
                    backgroundColor: 'var(--border-strong)',
                    flexGrow: 1,
                    minHeight: '40px',
                    margin: '4px 0'
                  }} />
                )}
              </div>

              {/* Stage Content Card */}
              <div style={{
                flexGrow: 1,
                padding: '16px 20px',
                backgroundColor: 'var(--surface)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                marginBottom: isLast ? '0' : '16px'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px', flexWrap: 'wrap', gap: '8px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <h3 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)', letterSpacing: '0.02em' }}>
                      {stage.name}
                    </h3>
                    <span className="mono-text" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                      [{stage.timestamp}]
                    </span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span className="mono-text" style={{ fontSize: '11px', color: 'var(--accent)' }}>
                      {stage.primaryHost}
                    </span>
                    <StatusBadge
                      type={stage.status === 'CONFIRMED' ? 'critical' : 'medium'}
                      label={stage.status}
                    />
                  </div>
                </div>

                <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '12px' }}>
                  {stage.summary}
                </p>

                {/* Telemetry Evidence Box */}
                <div style={{
                  padding: '10px 14px',
                  backgroundColor: 'var(--surface-elevated)',
                  borderRadius: 'var(--radius-xs)',
                  border: '1px solid var(--border)',
                  marginBottom: '10px'
                }}>
                  <div style={{ fontSize: '10px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', marginBottom: '3px' }}>
                    TELEMETRY EVIDENCE:
                  </div>
                  <div className="mono-text" style={{ fontSize: '12px', color: 'var(--text-primary)' }}>
                    {stage.evidence}
                  </div>
                </div>

                {/* Indicators Found */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)', marginRight: '4px' }}>IOCs:</span>
                  {stage.indicators.map((ind) => (
                    <span
                      key={ind}
                      className="mono-text"
                      style={{
                        padding: '2px 8px',
                        backgroundColor: 'var(--bg)',
                        border: '1px solid var(--border)',
                        borderRadius: 'var(--radius-xs)',
                        fontSize: '11px',
                        color: 'var(--text-secondary)'
                      }}
                    >
                      {ind}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
