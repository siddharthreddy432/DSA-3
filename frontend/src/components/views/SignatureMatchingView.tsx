import React, { useState } from 'react';
import { ALGORITHM_SPECS, SAMPLE_SIGNATURE_PATTERNS, SAMPLE_LOGS } from '../../data/sampleData';
import type { MatchingAlgorithm } from '../../types/engine';
import { SectionHeader } from '../common/SectionHeader';
import { StatusBadge } from '../common/StatusBadge';
import { Search, Code2, Cpu } from 'lucide-react';

export const SignatureMatchingView: React.FC = () => {
  const [selectedAlgo, setSelectedAlgo] = useState<MatchingAlgorithm>('KMP');
  const [searchPattern, setSearchPattern] = useState<string>('powershell -enc');

  const currentAlgo = ALGORITHM_SPECS.find((a) => a.id === selectedAlgo) || ALGORITHM_SPECS[0];

  // Perform simulated matching across sample logs
  const matches = SAMPLE_LOGS.filter((log) =>
    log.payload.toLowerCase().includes(searchPattern.toLowerCase()) ||
    log.rawDetails.toLowerCase().includes(searchPattern.toLowerCase())
  ).map((log, index) => {
    const text = log.payload;
    const offset = text.toLowerCase().indexOf(searchPattern.toLowerCase());
    return {
      id: `MATCH-${index + 1}`,
      logId: log.id,
      time: log.time,
      host: log.targetHost,
      offset: offset >= 0 ? offset : 0,
      length: searchPattern.length,
      snippet: text
    };
  });

  return (
    <div>
      <SectionHeader
        tag="STAGE 02 — STRING ALGORITHMS"
        title="Signature Matching Engine"
        description="Specification and visualization of sublinear string pattern matching algorithms planned for payload indicator scanning without standard Java regex libraries."
        actionRow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <StatusBadge type="planned" label="ALGORITHMIC SPECIFICATION (PHASE 4)" />
          </div>
        }
      />

      {/* Algorithm Selection Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '12px', marginBottom: '24px' }}>
        {ALGORITHM_SPECS.map((algo) => {
          const isSelected = algo.id === selectedAlgo;
          return (
            <div
              key={algo.id}
              onClick={() => setSelectedAlgo(algo.id)}
              data-clickable="true"
              style={{
                padding: '16px',
                backgroundColor: isSelected ? 'var(--surface-elevated)' : 'var(--surface)',
                border: `1px solid ${isSelected ? 'var(--accent)' : 'var(--border)'}`,
                borderRadius: 'var(--radius-sm)',
                cursor: 'pointer',
                transition: 'all var(--transition-fast)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                <span style={{ fontSize: '14px', fontWeight: 600, color: isSelected ? 'var(--text-primary)' : 'var(--text-secondary)' }}>
                  {algo.name}
                </span>
                <span className="tech-label" style={{ color: 'var(--accent)' }}>
                  {algo.complexityTime}
                </span>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                {algo.description}
              </p>
            </div>
          );
        })}
      </div>

      {/* Pattern Input & Presets */}
      <div style={{
        padding: '20px',
        backgroundColor: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius-sm)',
        marginBottom: '24px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px', flexWrap: 'wrap', gap: '8px' }}>
          <label htmlFor="pattern-input" style={{ fontSize: '12px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
            TARGET INDICATOR SIGNATURE:
          </label>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-muted)', marginRight: '4px' }}>Presets:</span>
            {SAMPLE_SIGNATURE_PATTERNS.slice(0, 4).map((p) => (
              <button
                key={p}
                className="btn-action"
                style={{ padding: '2px 8px', fontSize: '11px', fontFamily: 'var(--font-mono)' }}
                onClick={() => setSearchPattern(p)}
              >
                {p}
              </button>
            ))}
          </div>
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <div style={{
            position: 'relative',
            flexGrow: 1,
            display: 'flex',
            alignItems: 'center'
          }}>
            <Search size={14} color="var(--text-muted)" style={{ position: 'absolute', left: '12px' }} />
            <input
              id="pattern-input"
              type="text"
              value={searchPattern}
              onChange={(e) => setSearchPattern(e.target.value)}
              placeholder="Enter indicator pattern..."
              style={{
                width: '100%',
                padding: '9px 12px 9px 34px',
                backgroundColor: 'var(--surface-elevated)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                color: 'var(--text-primary)',
                fontFamily: 'var(--font-mono)',
                fontSize: '13px'
              }}
            />
          </div>
        </div>
      </div>

      {/* Algorithmic Concept & Structure Explanation */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
        gap: '16px',
        marginBottom: '24px'
      }}>
        <div style={{
          padding: '16px',
          backgroundColor: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <Cpu size={14} color="var(--accent)" />
            <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
              Preprocessing & Asymptotics
            </h4>
          </div>
          <div style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
            <div><strong>Time:</strong> <span className="mono-text">{currentAlgo.complexityTime}</span></div>
            <div><strong>Auxiliary Space:</strong> <span className="mono-text">{currentAlgo.complexitySpace}</span></div>
            <div style={{ marginTop: '6px' }}>{currentAlgo.preprocessingSummary}</div>
          </div>
        </div>

        <div style={{
          padding: '16px',
          backgroundColor: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <Code2 size={14} color="var(--accent)" />
            <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
              Zero-Library Verification Rule
            </h4>
          </div>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
            Java engine implementation strictly avoids <span className="mono-text">java.util.regex</span> and pattern compilation libraries. All sliding-window and LPS arrays are computed in pure primitive arrays.
          </p>
        </div>
      </div>

      {/* Matches Table */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
        <h3 style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-primary)' }}>
          Simulated Pattern Matches ({matches.length})
        </h3>
        <StatusBadge type="simulated" label="SIMULATED SCAN" />
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: '90px' }}>MATCH ID</th>
              <th style={{ width: '90px' }}>LOG TIME</th>
              <th style={{ width: '100px' }}>TARGET</th>
              <th style={{ width: '100px' }}>BYTE OFFSET</th>
              <th>SNIPPET / EVIDENCE</th>
            </tr>
          </thead>
          <tbody>
            {matches.length === 0 ? (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)' }}>
                  Zero matches identified for pattern: "{searchPattern}"
                </td>
              </tr>
            ) : (
              matches.map((m) => (
                <tr key={m.id}>
                  <td className="mono-text" style={{ color: 'var(--text-muted)' }}>{m.id}</td>
                  <td className="mono-text" style={{ color: 'var(--text-secondary)' }}>{m.time}</td>
                  <td className="mono-text" style={{ color: 'var(--accent)' }}>{m.host}</td>
                  <td className="mono-text" style={{ color: 'var(--text-primary)' }}>idx {m.offset}</td>
                  <td className="mono-text" style={{ color: 'var(--text-primary)' }}>
                    {m.snippet}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
