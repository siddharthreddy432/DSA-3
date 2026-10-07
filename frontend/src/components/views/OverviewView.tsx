import React from 'react';
import type { ViewMode } from '../../types/engine';
import { SectionHeader } from '../common/SectionHeader';
import { Metric } from '../common/Metric';
import { StatusBadge } from '../common/StatusBadge';
import { ArrowRight, CheckCircle2 } from 'lucide-react';

interface OverviewViewProps {
  onNavigate: (view: ViewMode) => void;
}

export const OverviewView: React.FC<OverviewViewProps> = ({ onNavigate }) => {
  const verifiedStructures = [
    { name: 'DynamicArray<T>', desc: 'Contiguous primitive Object array with amortized O(1) append and doubling resizing.' },
    { name: 'LinkedList<T>', desc: 'Singly linked Node<T> implementation maintaining O(1) head/tail references.' },
    { name: 'Stack<T>', desc: 'Strict LIFO stack backed by custom DynamicArray with boundary underflow protection.' },
    { name: 'Queue<T>', desc: 'Circular buffer with modulo pointer arithmetic ensuring true O(1) dequeue without shifting.' }
  ];

  const pipelineStages = [
    { id: 'ingestion' as ViewMode, step: '01', name: 'INGEST', label: 'Log Ingestion', status: 'SIMULATION', desc: 'Raw event stream ingestion & schema normalization' },
    { id: 'signatures' as ViewMode, step: '02', name: 'MATCH', label: 'Signature Matching', status: 'PLANNED', desc: 'Exact sublinear pattern scanning (KMP, Z-Algorithm, Rabin-Karp)' },
    { id: 'killchain' as ViewMode, step: '03', name: 'CORRELATE', label: 'Kill Chain', status: 'SIMULATION', desc: '5-stage progression attribution & multi-source evidence mapping' },
    { id: 'graph' as ViewMode, step: '04', name: 'GRAPH', label: 'Attack Graph', status: 'SIMULATION', desc: 'Directed lateral movement topology & reachable trajectory construction' },
    { id: 'chokepoints' as ViewMode, step: '05', name: 'ANALYZE', label: 'Choke-Point Analysis', status: 'PLANNED', desc: 'Tarjan articulation point and Edmonds-Karp Max-Flow / Min-Cut' },
    { id: 'monitoring' as ViewMode, step: '06', name: 'OPTIMIZE', label: 'Sensor Placement', status: 'PLANNED', desc: 'Path-Hitting Set dual approximation for sensor placement' },
    { id: 'alerts' as ViewMode, step: '07', name: 'ROUTE', label: 'Alert Prioritization', status: 'SIMULATION', desc: 'Binary Min/Max Heap priority queue alert scheduling' }
  ];

  return (
    <div>
      <SectionHeader
        tag="SYSTEM OVERVIEW"
        title="Threat-Intelligence Kill-Chain Correlation Engine"
        description="First-principles algorithmic platform designed to correlate multi-stage intrusion telemetry, reconstruct lateral movement topologies, and compute optimal network cut bottlenecks without third-party collection libraries."
      />

      {/* Engine Truth & Status Banner */}
      <div style={{
        padding: '16px 20px',
        backgroundColor: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius-sm)',
        marginBottom: '28px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '12px'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '11px', color: 'var(--text-muted)' }}>ENGINE STATUS</span>
            <StatusBadge type="verified" label="JAVA 17 CORE: PHASE 2 VERIFIED" />
          </div>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            Foundational data structures compiled and verified natively with 36/36 tests passing. Core algorithmic pipeline stages are currently in design preview mode.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button className="btn-action" onClick={() => onNavigate('ingestion')}>
            <span>Inspect Ingestion</span>
            <ArrowRight size={13} />
          </button>
          <button className="btn-action" onClick={() => onNavigate('graph')}>
            <span>View Attack Graph</span>
            <ArrowRight size={13} />
          </button>
        </div>
      </div>

      {/* Summary Metrics (Explicitly Marked as Demo Data) */}
      <div style={{ marginBottom: '12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <h3 style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-primary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          Simulation Metrics
        </h3>
        <StatusBadge type="demo" label="DEMO DATA" />
      </div>

      <div className="metrics-row">
        <Metric
          label="Logs Processed"
          value="12,482"
          subtext="Simulated event stream"
          badge={<span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--text-subtle)' }}>STREAM</span>}
        />
        <Metric
          label="Indicators Matched"
          value="347"
          subtext="Known signature signatures"
          badge={<span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--text-subtle)' }}>IOCS</span>}
        />
        <Metric
          label="Attack Paths"
          value="28"
          subtext="Enumerated multi-hop traces"
          badge={<span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--text-subtle)' }}>PATHS</span>}
        />
        <Metric
          label="Choke Points"
          value="7"
          subtext="Identified bridge pivot hosts"
          badge={<span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--accent)' }}>CRITICAL</span>}
        />
        <Metric
          label="Active Alerts"
          value="9"
          subtext="Severity prioritized queue"
          badge={<span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--severity-critical)' }}>PRIORITY</span>}
        />
      </div>

      {/* Verified Core Substructures Section */}
      <div style={{ marginTop: '36px', marginBottom: '36px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
          <div>
            <h3 style={{ fontSize: '15px', fontWeight: 500, color: 'var(--text-primary)' }}>
              Verified Core Data Structures (Java 17)
            </h3>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              Custom first-principles structures strictly avoiding java.util collections or third-party wrappers.
            </p>
          </div>
          <StatusBadge type="verified" label="PHASE 2 COMPLETE" />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '12px' }}>
          {verifiedStructures.map((struct) => (
            <div key={struct.name} style={{
              padding: '16px',
              backgroundColor: 'var(--surface)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius-sm)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                <CheckCircle2 size={14} color="var(--status-verified)" />
                <span className="mono-text" style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
                  {struct.name}
                </span>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                {struct.desc}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* Planned Correlation Pipeline Flow */}
      <div style={{ marginTop: '40px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
          <div>
            <h3 style={{ fontSize: '15px', fontWeight: 500, color: 'var(--text-primary)' }}>
              Algorithmic Engine Pipeline
            </h3>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              End-to-end multi-phase architecture from raw telemetry to alert routing.
            </p>
          </div>
          <span className="tech-label" style={{ color: 'var(--text-muted)' }}>STAGES 01 – 07</span>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {pipelineStages.map((stage) => (
            <div
              key={stage.step}
              onClick={() => onNavigate(stage.id)}
              data-clickable="true"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '12px 16px',
                backgroundColor: 'var(--surface)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                cursor: 'pointer',
                transition: 'background-color var(--transition-fast), border-color var(--transition-fast)'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = 'var(--surface-hover)';
                e.currentTarget.style.borderColor = 'var(--border-strong)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = 'var(--surface)';
                e.currentTarget.style.borderColor = 'var(--border)';
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                <span className="mono-text" style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  {stage.step}
                </span>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
                      {stage.label}
                    </span>
                    <span className="mono-text" style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                      [{stage.name}]
                    </span>
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                    {stage.desc}
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <StatusBadge
                  type={stage.status === 'SIMULATION' ? 'simulated' : 'planned'}
                  label={stage.status}
                />
                <ArrowRight size={14} color="var(--text-muted)" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
