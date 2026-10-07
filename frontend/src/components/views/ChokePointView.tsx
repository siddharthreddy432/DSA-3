import React from 'react';
import { CHOKE_POINTS } from '../../data/sampleData';
import { SectionHeader } from '../common/SectionHeader';
import { StatusBadge } from '../common/StatusBadge';
import { Network, GitBranch } from 'lucide-react';

export const ChokePointView: React.FC = () => {
  return (
    <div>
      <SectionHeader
        tag="STAGE 05 — STRUCTURAL ANALYSIS"
        title="Choke-Point & Bottleneck Analysis"
        description="Graph partitioning models designed to identify single points of lateral failure (Articulation Points) and minimal edge cut sets (Min-Cut) separating ingress hosts from crown jewel databases."
        actionRow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <StatusBadge type="planned" label="ALGORITHMIC SPECIFICATION (PHASE 6)" />
          </div>
        }
      />

      {/* Algorithmic Principles Overview */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
        gap: '16px',
        marginBottom: '28px'
      }}>
        <div style={{
          padding: '20px',
          backgroundColor: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <GitBranch size={16} color="var(--accent)" />
            <h3 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>
              Tarjan-Style Articulation Points
            </h3>
          </div>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.6', marginBottom: '8px' }}>
            Visits every vertex in single DFS pass using discovery time (<span className="mono-text">disc[]</span>) and low-link tracking (<span className="mono-text">low[]</span>). A node <span className="mono-text">u</span> is an articulation bridge if any child subtree cannot reach an ancestor of <span className="mono-text">u</span> without passing through <span className="mono-text">u</span>.
          </p>
          <div className="mono-text" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
            Complexity: O(V + E) | Space: O(V)
          </div>
        </div>

        <div style={{
          padding: '20px',
          backgroundColor: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <Network size={16} color="var(--accent)" />
            <h3 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>
              Edmonds-Karp Max-Flow / Min-Cut
            </h3>
          </div>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.6', marginBottom: '8px' }}>
            Augmenting path BFS traversal on residual capacity matrices. By the Max-Flow Min-Cut Theorem, saturated bottlenecks reveal the minimal capacity cut edge set separating the source partition from the target sink.
          </p>
          <div className="mono-text" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
            Complexity: O(V · E²) | Space: O(V²)
          </div>
        </div>
      </div>

      {/* Critical Choke Points Table */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
        <h3 style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-primary)' }}>
          Ranked Critical Choke Points (Simulated Model)
        </h3>
        <StatusBadge type="simulated" label="SIMULATED ANALYSIS" />
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: '110px' }}>HOST ID</th>
              <th style={{ width: '130px' }}>IP ADDRESS</th>
              <th>SYSTEM ROLE</th>
              <th style={{ width: '140px' }}>REACHABLE PATHS</th>
              <th style={{ width: '110px' }}>MIN-CUT CAP</th>
              <th style={{ width: '160px', textAlign: 'right' }}>BOTTLENECK STATUS</th>
            </tr>
          </thead>
          <tbody>
            {CHOKE_POINTS.map((cp) => (
              <tr key={cp.hostId}>
                <td className="mono-text" style={{ color: 'var(--accent)', fontWeight: 600 }}>{cp.hostId}</td>
                <td className="mono-text" style={{ color: 'var(--text-secondary)' }}>{cp.ip}</td>
                <td style={{ color: 'var(--text-primary)' }}>{cp.role}</td>
                <td className="mono-text" style={{ color: cp.reachablePaths >= 7 ? 'var(--severity-critical)' : 'var(--text-primary)' }}>
                  {cp.reachablePaths} / {cp.totalPaths} paths ({(cp.reachablePaths / cp.totalPaths * 100).toFixed(0)}%)
                </td>
                <td className="mono-text" style={{ color: 'var(--text-secondary)' }}>
                  {cp.minCutCapacity} units
                </td>
                <td style={{ textAlign: 'right' }}>
                  <StatusBadge
                    type={cp.cutStatus === 'PRIMARY_BOTTLENECK' ? 'critical' : 'high'}
                    label={cp.cutStatus.replace('_', ' ')}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div style={{ marginTop: '16px', padding: '12px 16px', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--surface)' }}>
        <p style={{ fontSize: '11px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
          [NOTICE] Values presented above represent the algorithmic mathematical model for Phase 6. The core Java graph classes and articulation point traversals will be implemented directly in the Java engine without third-party graph libraries.
        </p>
      </div>
    </div>
  );
};
