import React from 'react';
import { MONITORING_RECOMMENDATIONS } from '../../data/sampleData';
import { SectionHeader } from '../common/SectionHeader';
import { StatusBadge } from '../common/StatusBadge';
import { Eye } from 'lucide-react';

export const MonitoringView: React.FC = () => {
  return (
    <div>
      <SectionHeader
        tag="STAGE 06 — SENSOR OPTIMIZATION"
        title="Monitoring & Sensor Placement"
        description="Path-Hitting Set / Set Cover dual optimization designed to compute the minimal sensor placement set guaranteeing that every valid multi-hop attack path contains at least one telemetry probe."
        actionRow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <StatusBadge type="planned" label="OPTIMIZATION SPECIFICATION (PHASE 6)" />
          </div>
        }
      />

      {/* Theoretical Model Note */}
      <div style={{
        padding: '16px 20px',
        backgroundColor: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius-sm)',
        marginBottom: '24px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
          <Eye size={15} color="var(--accent)" />
          <h3 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
            Path-Hitting Set Formulation
          </h3>
        </div>
        <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
          A standard Vertex Cover only covers single edges, which does not guarantee monitoring all multi-hop directed attack trajectories. The engine formulates sensor allocation as a Path Hitting Set over the set of enumerated valid attack paths <span className="mono-text">P ∈ 𝒫</span>, selecting sensor hosts to maximize coverage with minimal monitoring overhead.
        </p>
      </div>

      {/* Sensor Recommendations Table */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
        <h3 style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-primary)' }}>
          Ranked Monitoring Host Placements
        </h3>
        <StatusBadge type="simulated" label="SIMULATION" />
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: '60px' }}>RANK</th>
              <th style={{ width: '220px' }}>RECOMMENDED SENSOR HOST</th>
              <th style={{ width: '130px' }}>PATHS COVERED</th>
              <th style={{ width: '110px' }}>COVERAGE %</th>
              <th style={{ width: '140px' }}>SENSOR TYPE</th>
              <th>OPTIMIZATION RATIONALE</th>
            </tr>
          </thead>
          <tbody>
            {MONITORING_RECOMMENDATIONS.map((rec) => (
              <tr key={rec.rank}>
                <td className="mono-text" style={{ color: 'var(--text-muted)' }}>#{rec.rank}</td>
                <td className="mono-text" style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{rec.sensorHost}</td>
                <td className="mono-text" style={{ color: 'var(--accent)' }}>
                  {rec.pathsCovered} / {rec.totalPaths}
                </td>
                <td className="mono-text" style={{ color: 'var(--text-primary)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span>{rec.coveragePercentage}%</span>
                    <div style={{
                      width: '40px',
                      height: '4px',
                      backgroundColor: 'var(--surface-elevated)',
                      borderRadius: '2px',
                      overflow: 'hidden'
                    }}>
                      <div style={{
                        width: `${rec.coveragePercentage}%`,
                        height: '100%',
                        backgroundColor: 'var(--accent)'
                      }} />
                    </div>
                  </div>
                </td>
                <td>
                  <span className="tech-label" style={{
                    padding: '2px 6px',
                    backgroundColor: 'var(--surface-elevated)',
                    border: '1px solid var(--border)',
                    borderRadius: 'var(--radius-xs)'
                  }}>
                    {rec.sensorType}
                  </span>
                </td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>
                  {rec.rationale}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div style={{ marginTop: '16px', padding: '12px 16px', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--surface)' }}>
        <p style={{ fontSize: '11px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
          [NOTICE] Sensor coverage percentages above represent simulated dual set-cover output. The algorithm will be implemented natively in the Java core in Phase 6.
        </p>
      </div>
    </div>
  );
};
