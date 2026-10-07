import React, { useState } from 'react';
import { SAMPLE_LOGS, DEMO_SCENARIO_NAME } from '../../data/sampleData';
import type { LogCategory, LogEntry } from '../../types/engine';
import { SectionHeader } from '../common/SectionHeader';
import { StatusBadge } from '../common/StatusBadge';
import { Filter, ChevronRight, ChevronDown } from 'lucide-react';

export const IngestionView: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = useState<LogCategory>('ALL');
  const [selectedScenario, setSelectedScenario] = useState<string>('apt29');
  const [expandedLogId, setExpandedLogId] = useState<string | null>(null);

  const categories: LogCategory[] = ['ALL', 'AUTH', 'EXPLOIT', 'PRIVILEGE', 'LATERAL', 'EXFIL'];

  const filteredLogs = SAMPLE_LOGS.filter((log) => {
    if (selectedCategory === 'ALL') return true;
    return log.category === selectedCategory;
  });

  const toggleExpand = (id: string) => {
    setExpandedLogId(expandedLogId === id ? null : id);
  };

  return (
    <div>
      <SectionHeader
        tag="STAGE 01 — TELEMETRY INGESTION"
        title="Log Ingestion Stream"
        description="Stream ingestion and normalization interface. Parses heterogeneous auth, execution, network transit, and database audit events into structured event payloads for correlation analysis."
        actionRow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <StatusBadge type="simulated" label="SIMULATED DATASET" />
          </div>
        }
      />

      {/* Scenario & Controls Bar */}
      <div className="filter-bar" style={{ padding: '12px 16px', backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
          <span className="tech-label" style={{ color: 'var(--text-muted)' }}>DEMO SCENARIO:</span>
          <select
            value={selectedScenario}
            onChange={(e) => setSelectedScenario(e.target.value)}
            style={{
              backgroundColor: 'var(--surface-elevated)',
              color: 'var(--text-primary)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius-xs)',
              padding: '4px 10px',
              fontFamily: 'var(--font-mono)',
              fontSize: '12px'
            }}
          >
            <option value="apt29">APT-29 Lateral Movement & Exfiltration (9 events)</option>
            <option value="ransomware">Ransomware Pre-Execution Probe (Simulated)</option>
          </select>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span className="mono-text" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
            Showing {filteredLogs.length} of {SAMPLE_LOGS.length} records
          </span>
        </div>
      </div>

      {/* Category Filter Pills */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', margin: '16px 0 20px', flexWrap: 'wrap' }}>
        <span style={{ fontSize: '12px', color: 'var(--text-muted)', marginRight: '6px', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
          <Filter size={12} /> Filter:
        </span>
        {categories.map((cat) => (
          <button
            key={cat}
            className={`btn-action ${selectedCategory === cat ? 'active' : ''}`}
            onClick={() => setSelectedCategory(cat)}
            style={{ padding: '4px 10px', fontSize: '11px', fontFamily: 'var(--font-mono)' }}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Log Events Table */}
      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: '32px' }}></th>
              <th style={{ width: '90px' }}>TIME</th>
              <th style={{ width: '130px' }}>SOURCE IP</th>
              <th style={{ width: '110px' }}>TARGET</th>
              <th style={{ width: '100px' }}>CATEGORY</th>
              <th>EVENT PAYLOAD</th>
              <th style={{ width: '100px', textAlign: 'right' }}>SEVERITY</th>
            </tr>
          </thead>
          <tbody>
            {filteredLogs.map((log: LogEntry) => {
              const isExpanded = expandedLogId === log.id;
              return (
                <React.Fragment key={log.id}>
                  <tr
                    onClick={() => toggleExpand(log.id)}
                    style={{ cursor: 'pointer' }}
                    data-clickable="true"
                  >
                    <td style={{ textAlign: 'center', padding: '8px 4px' }}>
                      {isExpanded ? <ChevronDown size={14} color="var(--text-muted)" /> : <ChevronRight size={14} color="var(--text-muted)" />}
                    </td>
                    <td className="mono-text" style={{ color: 'var(--text-secondary)' }}>{log.time}</td>
                    <td className="mono-text" style={{ color: 'var(--text-primary)' }}>{log.sourceIp}</td>
                    <td className="mono-text" style={{ color: 'var(--accent)' }}>{log.targetHost}</td>
                    <td>
                      <span className="tech-label" style={{
                        padding: '2px 6px',
                        backgroundColor: 'var(--surface-elevated)',
                        borderRadius: 'var(--radius-xs)',
                        border: '1px solid var(--border)'
                      }}>
                        {log.category}
                      </span>
                    </td>
                    <td className="mono-text" style={{ color: 'var(--text-primary)', wordBreak: 'break-word' }}>
                      {log.payload}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <StatusBadge
                        type={log.severity.toLowerCase() as any}
                        label={log.severity}
                      />
                    </td>
                  </tr>

                  {isExpanded && (
                    <tr style={{ backgroundColor: 'var(--surface-elevated)' }}>
                      <td colSpan={7} style={{ padding: '16px 20px', borderBottom: '1px solid var(--border)' }}>
                        <div style={{ display: 'grid', gridTemplateColumns: 'auto 1fr', gap: '8px 16px', fontSize: '12px' }}>
                          <span className="mono-text" style={{ color: 'var(--text-muted)' }}>LOG ID:</span>
                          <span className="mono-text" style={{ color: 'var(--text-primary)' }}>{log.id}</span>

                          <span className="mono-text" style={{ color: 'var(--text-muted)' }}>DETAILS:</span>
                          <span style={{ color: 'var(--text-secondary)' }}>{log.rawDetails}</span>

                          <span className="mono-text" style={{ color: 'var(--text-muted)' }}>NORMALIZATION:</span>
                          <span className="mono-text" style={{ color: 'var(--status-verified)' }}>Parsed into structured schema; correlated with kill-chain timeline.</span>
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              );
            })}
          </tbody>
        </table>
      </div>

      <div style={{ marginTop: '16px', padding: '12px 16px', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--surface)' }}>
        <p style={{ fontSize: '11px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
          [NOTICE] Raw logs shown above are synthesized demonstration telemetry representing the {DEMO_SCENARIO_NAME}. In Phase 4, the Java engine log parser will ingest lines directly from data/input/logs.txt without external dependencies.
        </p>
      </div>
    </div>
  );
};
